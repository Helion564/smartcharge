package com.smartcharge.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE    = "role";

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long accessTokenExpirationMs) {

        // Encode the secret to Base64 to build a 256-bit minimum key
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /**
     * Generate a signed JWT access token from the given UserDetails.
     * The UserDetails must be an instance of {@link com.smartcharge.user.entity.User}.
     */
    public String generateAccessToken(UserDetails userDetails) {
        com.smartcharge.user.entity.User user = (com.smartcharge.user.entity.User) userDetails;

        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generate a random UUID string to serve as an opaque refresh token.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Validate a JWT token signature and expiry.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token is expired: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("JWT token signature is invalid: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT token is malformed: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT token is empty or null: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extract the subject (email) from the token.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract the userId claim from the token.
     */
    public UUID extractUserId(String token) {
        String userIdStr = parseClaims(token).get(CLAIM_USER_ID, String.class);
        return UUID.fromString(userIdStr);
    }

    /**
     * Extract the role claim from the token.
     */
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    // ---- private helpers ----

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
