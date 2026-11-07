package com.smartcharge.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcharge.payment.enums.PaymentMethod;
import com.smartcharge.payment.enums.PaymentStatus;
import com.smartcharge.payment.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full payment details returned to the client.
 * Amounts are formatted to 2 decimal places for consistency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private String userId;
    private String sessionId;
    private String bookingId;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    /** Base amount before tax (INR). */
    private double amount;

    /** GST tax amount (18% of base amount, INR). */
    private double taxAmount;

    /** Total charged including tax (INR). */
    private double totalAmount;

    /** Amount refunded so far (INR). */
    private double refundedAmount;

    /** Amount still outstanding after refunds (INR). */
    private double netAmount;

    private String currency;
    private String transactionId;
    private String gatewayTransactionId;
    private String description;
    private String failureReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;
}
