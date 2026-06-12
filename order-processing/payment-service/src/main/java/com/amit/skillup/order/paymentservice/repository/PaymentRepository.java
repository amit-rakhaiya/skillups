package com.amit.skillup.order.paymentservice.repository;

import com.amit.skillup.order.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}
