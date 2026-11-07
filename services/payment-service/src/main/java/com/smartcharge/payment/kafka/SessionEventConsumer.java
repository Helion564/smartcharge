package com.smartcharge.payment.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcharge.payment.dto.PaymentRequest;
import com.smartcharge.payment.enums.PaymentMethod;
import com.smartcharge.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "session-events", groupId = "payment-service-group")
    public void handleSessionEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.path("eventType").asText();

            if ("SESSION_COMPLETED".equalsIgnoreCase(eventType)) {
                String sessionId = node.path("sessionId").asText(null);
                String userId = node.path("userId").asText(null);
                double totalCost = node.path("totalCost").asDouble(0.0);

                if (sessionId != null && userId != null && totalCost > 0) {
                    log.info("Auto-charging completed session {} for user {}: INR {}", sessionId, userId, totalCost);
                    PaymentRequest req = PaymentRequest.builder()
                            .sessionId(sessionId)
                            .amount(totalCost)
                            .paymentMethod(PaymentMethod.SMARTCHARGE_WALLET)
                            .description("Auto-debit for completed charging session " + sessionId)
                            .build();

                    try {
                        paymentService.processPayment(userId, req);
                    } catch (Exception ex) {
                        log.warn("Wallet debit failed, fallback to UPI pending: {}", ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing session event in payment service: {}", e.getMessage());
        }
    }
}
