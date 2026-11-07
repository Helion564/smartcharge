package com.smartcharge.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryResponse {
    private double totalRevenue;
    private long totalTransactions;
    private double successRate;
    private double avgTransactionAmount;
}
