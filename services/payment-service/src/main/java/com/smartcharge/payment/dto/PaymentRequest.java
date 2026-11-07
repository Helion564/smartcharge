package com.smartcharge.payment.dto;

import com.smartcharge.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for initiating a new payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /** Optional: ID of the charging session this payment is for. */
    private String sessionId;

    /** Optional: ID of the booking this payment is for. */
    private String bookingId;

    /** Base amount to charge (before tax), in INR. Must be positive. */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private Double amount;

    /** Payment method the user wishes to use. */
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /** Human-readable description of what this payment covers. */
    private String description;
}
