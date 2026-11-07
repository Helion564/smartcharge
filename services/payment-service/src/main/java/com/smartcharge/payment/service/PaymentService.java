package com.smartcharge.payment.service;

import com.smartcharge.payment.dto.*;
import com.smartcharge.payment.entity.Payment;
import com.smartcharge.payment.entity.Wallet;
import com.smartcharge.payment.entity.WalletTransaction;
import com.smartcharge.payment.enums.PaymentMethod;
import com.smartcharge.payment.enums.PaymentStatus;
import com.smartcharge.payment.enums.PaymentType;
import com.smartcharge.payment.repository.PaymentRepository;
import com.smartcharge.payment.repository.WalletRepository;
import com.smartcharge.payment.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-events";
    private static final double GST_RATE = 0.18; // 18% GST in India

    @Transactional
    public PaymentResponse processPayment(String userId, PaymentRequest request) {
        double amount = request.getAmount();
        double taxAmount = Math.round(amount * GST_RATE * 100.0) / 100.0;
        double totalAmount = Math.round((amount + taxAmount) * 100.0) / 100.0;

        String txId = "SC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String gatewayTxId = "MOCK-GW-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        PaymentType type = PaymentType.SESSION_CHARGE;
        if (request.getBookingId() != null && request.getSessionId() == null) {
            type = PaymentType.BOOKING_CHARGE;
        }

        Payment payment = Payment.builder()
                .userId(userId)
                .sessionId(request.getSessionId())
                .bookingId(request.getBookingId())
                .paymentType(type)
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .transactionId(txId)
                .gatewayTransactionId(gatewayTxId)
                .description(request.getDescription())
                .build();

        // Handle SMARTCHARGE_WALLET debit
        if (request.getPaymentMethod() == PaymentMethod.SMARTCHARGE_WALLET) {
            Wallet wallet = getOrCreateWallet(userId);
            if (wallet.getBalance() < totalAmount) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Insufficient wallet balance. Available: INR " + wallet.getBalance());
                paymentRepository.save(payment);
                publishPaymentEvent("PAYMENT_FAILED", payment);
                throw new IllegalStateException("Insufficient wallet balance");
            }
            wallet.setBalance(wallet.getBalance() - totalAmount);
            wallet.setTotalCharged(wallet.getTotalCharged() + totalAmount);
            walletRepository.save(wallet);

            WalletTransaction wt = WalletTransaction.builder()
                    .wallet(wallet)
                    .amount(totalAmount)
                    .type("DEBIT")
                    .description("Payment for EV charging: " + txId)
                    .referenceId(txId)
                    .build();
            walletTransactionRepository.save(wt);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        publishPaymentEvent("PAYMENT_SUCCESS", saved);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId, String userId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (!isAdmin && !payment.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized to view this payment");
        }

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(String userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public PaymentResponse initiateRefund(RefundRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new IllegalStateException("Payment cannot be refunded in current status: " + payment.getStatus());
        }

        double remaining = payment.getTotalAmount() - payment.getRefundedAmount();
        if (request.getRefundAmount() > remaining) {
            throw new IllegalArgumentException("Refund amount exceeds remaining non-refunded amount");
        }

        payment.setRefundedAmount(payment.getRefundedAmount() + request.getRefundAmount());
        if (payment.getRefundedAmount() >= payment.getTotalAmount()) {
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        Payment saved = paymentRepository.save(payment);

        // Credit to wallet if user used wallet
        if (payment.getPaymentMethod() == PaymentMethod.SMARTCHARGE_WALLET) {
            Wallet wallet = getOrCreateWallet(payment.getUserId());
            wallet.setBalance(wallet.getBalance() + request.getRefundAmount());
            wallet.setTotalRefunded(wallet.getTotalRefunded() + request.getRefundAmount());
            walletRepository.save(wallet);

            WalletTransaction wt = WalletTransaction.builder()
                    .wallet(wallet)
                    .amount(request.getRefundAmount())
                    .type("CREDIT")
                    .description("Refund for tx: " + payment.getTransactionId())
                    .referenceId(payment.getTransactionId())
                    .build();
            walletTransactionRepository.save(wt);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public WalletResponse getWallet(String userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return mapToWalletResponse(wallet);
    }

    @Transactional
    public WalletResponse topupWallet(String userId, WalletTopupRequest request) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + request.getAmount());
        Wallet saved = walletRepository.save(wallet);

        String refId = "TOPUP-" + System.currentTimeMillis();
        WalletTransaction wt = WalletTransaction.builder()
                .wallet(saved)
                .amount(request.getAmount())
                .type("CREDIT")
                .description("Wallet top-up via " + request.getPaymentMethod())
                .referenceId(refId)
                .build();
        walletTransactionRepository.save(wt);

        return mapToWalletResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummary() {
        List<Payment> all = paymentRepository.findAll();
        long count = all.size();
        if (count == 0) {
            return PaymentSummaryResponse.builder().totalRevenue(0).totalTransactions(0).successRate(100).avgTransactionAmount(0).build();
        }

        double totalRevenue = all.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS || p.getStatus() == PaymentStatus.PARTIALLY_REFUNDED)
                .mapToDouble(Payment::getTotalAmount)
                .sum();

        long successCount = all.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS || p.getStatus() == PaymentStatus.PARTIALLY_REFUNDED)
                .count();

        double successRate = Math.round((double) successCount / count * 10000.0) / 100.0;
        double avg = Math.round((totalRevenue / (successCount > 0 ? successCount : 1)) * 100.0) / 100.0;

        return PaymentSummaryResponse.builder()
                .totalRevenue(Math.round(totalRevenue * 100.0) / 100.0)
                .totalTransactions(count)
                .successRate(successRate)
                .avgTransactionAmount(avg)
                .build();
    }

    private Wallet getOrCreateWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(Wallet.builder().userId(userId).balance(500.0).build())); // Give ₹500 welcome credit
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .sessionId(p.getSessionId())
                .bookingId(p.getBookingId())
                .paymentType(p.getPaymentType())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .amount(p.getAmount())
                .taxAmount(p.getTaxAmount())
                .totalAmount(p.getTotalAmount())
                .refundedAmount(p.getRefundedAmount())
                .netAmount(p.getTotalAmount() - p.getRefundedAmount())
                .currency(p.getCurrency())
                .transactionId(p.getTransactionId())
                .gatewayTransactionId(p.getGatewayTransactionId())
                .description(p.getDescription())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .completedAt(p.getCompletedAt())
                .build();
    }

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        List<WalletTransaction> txs = walletTransactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId(), PageRequest.of(0, 10));

        List<WalletResponse.TransactionSummary> summaries = txs.stream()
                .map(t -> WalletResponse.TransactionSummary.builder()
                        .id(t.getId())
                        .amount(t.getAmount())
                        .type(t.getType())
                        .description(t.getDescription())
                        .referenceId(t.getReferenceId())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .totalCharged(wallet.getTotalCharged())
                .totalRefunded(wallet.getTotalRefunded())
                .recentTransactions(summaries)
                .build();
    }

    private void publishPaymentEvent(String eventType, Payment payment) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("eventType", eventType);
            map.put("paymentId", payment.getId());
            map.put("userId", payment.getUserId());
            map.put("amount", payment.getTotalAmount());
            map.put("status", payment.getStatus().name());
            map.put("transactionId", payment.getTransactionId());
            map.put("timestamp", System.currentTimeMillis());
            kafkaTemplate.send(PAYMENT_TOPIC, payment.getId().toString(), map);
        } catch (Exception e) {
            log.error("Failed to publish payment Kafka event: {}", e.getMessage());
        }
    }
}
