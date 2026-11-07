package com.smartcharge.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response representing a user's SmartCharge Wallet state,
 * including recent transaction history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private UUID id;
    private String userId;

    /** Current available balance (INR). */
    private double balance;

    /** Total amount ever debited from this wallet (INR). */
    private double totalCharged;

    /** Total amount ever credited/refunded to this wallet (INR). */
    private double totalRefunded;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /** Most recent transactions (up to 10) for quick overview. */
    private List<WalletTransactionResponse> recentTransactions;

    /**
     * Lightweight representation of a single wallet transaction.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletTransactionResponse {
        private UUID id;
        private double amount;
        private String type;
        private String description;
        private String referenceId;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
}
