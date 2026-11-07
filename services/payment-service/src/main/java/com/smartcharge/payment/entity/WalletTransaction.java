package com.smartcharge.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records every individual credit or debit transaction against a Wallet.
 * Provides a full audit trail of wallet balance changes.
 */
@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wallet_tx_wallet_id", columnList = "wallet_id"),
        @Index(name = "idx_wallet_tx_reference_id", columnList = "referenceId"),
        @Index(name = "idx_wallet_tx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"wallet"})
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** The wallet this transaction belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    /** The monetary amount of this transaction (in INR). */
    @Column(nullable = false)
    private double amount;

    /**
     * Direction of fund flow.
     * CREDIT = money added to wallet.
     * DEBIT  = money deducted from wallet.
     */
    @Column(nullable = false, length = 10)
    private String type;

    /** Human-readable explanation of this transaction. */
    @Column(nullable = true, length = 500)
    private String description;

    /** External reference ID (payment ID, booking ID, session ID, etc.). */
    @Column(nullable = true)
    private String referenceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
