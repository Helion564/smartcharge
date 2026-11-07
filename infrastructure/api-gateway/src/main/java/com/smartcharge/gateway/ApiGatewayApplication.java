package com.smartcharge.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * SmartCharge API Gateway.
 *
 * <p>Single entry-point for all client requests. Responsibilities:
 * <ul>
 *   <li>JWT authentication (via {@link com.smartcharge.gateway.filter.JwtAuthenticationFilter})</li>
 *   <li>Route requests to downstream microservices via Eureka service discovery</li>
 *   <li>CORS handling</li>
 *   <li>Expose Prometheus metrics at /actuator/prometheus</li>
 * </ul>
 *
 * <p>Built on Spring Cloud Gateway (reactive/WebFlux) — does NOT include spring-webmvc.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
