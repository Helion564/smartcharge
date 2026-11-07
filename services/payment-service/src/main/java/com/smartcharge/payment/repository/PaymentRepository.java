package com.smartcharge.payment.repository;

import com.smartcharge.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    Optional<Payment> findByBookingId(String bookingId);

    Optional<Payment> findByTransactionId(String transactionId);
}
