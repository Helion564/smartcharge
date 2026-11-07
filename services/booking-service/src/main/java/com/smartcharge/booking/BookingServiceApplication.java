package com.smartcharge.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Booking Service microservice.
 * Handles EV charging slot reservations, conflict detection via Redis distributed locks,
 * and event-driven communication with other services via Kafka.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableScheduling
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
