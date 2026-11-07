package com.smartcharge.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user's SmartCharge Wallet — an internal balance store
 * that can be topped up and used for payments.
 */
@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_user_id", columnList = "userId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** The user who owns this wallet. Each user has exactly one wallet. */
    @Column(nullable = false, unique = true)
    private String userId;

    /** Current available balance in INR. */
    @Column(nullable = false)
    @Builder.Default
    private double balance = 0.0;

    /** Cumulative total amount debited from this wallet over its lifetime. */
    @Column(nullable = false)
    @Builder.Default
    private double totalCharged = 0.0;

    /** Cumulative total amount refunded/credited to this wallet over its lifetime. */
    @Column(nullable = false)
    @Builder.Default
    private double totalRefunded = 0.0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
