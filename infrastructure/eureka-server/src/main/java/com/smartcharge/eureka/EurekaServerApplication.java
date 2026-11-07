package com.smartcharge.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * SmartCharge Service Registry.
 *
 * <p>All microservices register here on startup. The API Gateway uses this
 * registry for client-side load-balancing via Spring Cloud LoadBalancer (lb://).
 *
 * <p>Dashboard available at http://localhost:8761 when running locally.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
