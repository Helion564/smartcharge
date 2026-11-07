package com.smartcharge.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:smartcharge-super-secret-jwt-key-2024-minimum-256-bits-long-key-here}")
    private String jwtSecret;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/stations/nearby",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Allow public paths without token
        if (isExcluded(path)) {
            return chain.filter(exchange);
        }

        // Allow GET /api/v1/stations/** without token for public browsing
        if (request.getMethod().name().equalsIgnoreCase("GET") && path.startsWith("/api/v1/stations")) {
            // If token present, still parse it to pass down identity
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    Claims claims = extractClaims(authHeader.substring(7));
                    ServerHttpRequest mutatedRequest = mutateRequestWithClaims(request, claims);
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                } catch (Exception ignored) {
                    // ignore for optional auth
                }
            }
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = extractClaims(token);
            ServerHttpRequest mutatedRequest = mutateRequestWithClaims(request, claims);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return onError(exchange, "Invalid or expired JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private Claims extractClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private ServerHttpRequest mutateRequestWithClaims(ServerHttpRequest request, Claims claims) {
        String userId = claims.get("userId", String.class);
        String role = claims.get("role", String.class);
        String email = claims.getSubject();

        ServerHttpRequest.Builder builder = request.mutate();
        if (userId != null) {
            builder.header("X-User-Id", userId);
        }
        if (role != null) {
            builder.header("X-User-Role", role);
        }
        if (email != null) {
            builder.header("X-User-Email", email);
        }
        return builder.build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String json = String.format("{\"error\":\"%s\",\"status\":%d}", err, httpStatus.value());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
