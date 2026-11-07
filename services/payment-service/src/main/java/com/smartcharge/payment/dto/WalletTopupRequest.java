package com.smartcharge.payment.dto;

import com.smartcharge.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTopupRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10.0", message = "Minimum top-up amount is INR 10")
    private Double amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}

