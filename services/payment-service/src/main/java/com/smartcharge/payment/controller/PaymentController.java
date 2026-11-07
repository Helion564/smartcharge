package com.smartcharge.payment.controller;

import com.smartcharge.payment.dto.*;
import com.smartcharge.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(userId, request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID paymentId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId, userId, isAdmin));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<PaymentResponse>> getUserPayments(
            @RequestHeader("X-User-Id") String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(paymentService.getUserPayments(userId, pageable));
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> initiateRefund(
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.initiateRefund(request));
    }

    @GetMapping("/wallet")
    public ResponseEntity<WalletResponse> getWallet(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(paymentService.getWallet(userId));
    }

    @PostMapping("/wallet/topup")
    public ResponseEntity<WalletResponse> topupWallet(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody WalletTopupRequest request) {
        return ResponseEntity.ok(paymentService.topupWallet(userId, request));
    }

    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryResponse> getSummary() {
        return ResponseEntity.ok(paymentService.getPaymentSummary());
    }
}
