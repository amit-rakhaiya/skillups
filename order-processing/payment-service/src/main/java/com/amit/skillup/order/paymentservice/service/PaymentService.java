package com.amit.skillup.order.paymentservice.service;

import com.amit.skillup.order.common.event.PaymentEvent;
import com.amit.skillup.order.paymentservice.entity.Payment;
import com.amit.skillup.order.paymentservice.kafka.PaymentEventProducer;
import com.amit.skillup.order.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;

    /**
     * Simulates payment processing.
     * POC rule: amount > 10000 → fails (simulate insufficient funds).
     * Idempotent: if payment already exists for orderId, return existing result.
     */
    @Transactional
    public PaymentEvent processPayment(String orderId, String customerId, BigDecimal amount) {
        // Idempotency check
        if (paymentRepository.existsByOrderId(orderId)) {
            log.warn("Duplicate process-payment call for orderId {}, skipping", orderId);
            Payment existing = paymentRepository.findByOrderId(orderId).orElseThrow();
            return buildEvent(existing, "SUCCESS".equals(existing.getStatus()));
        }

        // POC: fail if amount > 10000
        boolean success = amount.compareTo(BigDecimal.valueOf(10000)) <= 0;
        String failureReason = success ? null : "Insufficient credit limit";

        Payment payment = Payment.builder()
            .orderId(orderId)
            .customerId(customerId)
            .amount(amount)
            .status(success ? "SUCCESS" : "FAILED")
            .failureReason(failureReason)
            .build();

        paymentRepository.save(payment);
        log.info("Payment {} for order {}: {}", payment.getId(), orderId, payment.getStatus());

        PaymentEvent event = buildEvent(payment, success);
        eventProducer.publishPaymentProcessed(event);
        return event;
    }

    /**
     * Compensation: refund a previously successful payment.
     */
    @Transactional
    public void refundPayment(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.setStatus("REFUNDED");
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
            log.info("Payment refunded for order {}", orderId);

            PaymentEvent refundEvent = buildEvent(payment, true);
            refundEvent = PaymentEvent.builder()
                .orderId(orderId)
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .success(true)
                .failureReason("Refunded")
                .build();
            eventProducer.publishPaymentProcessed(refundEvent);
        });
    }

    private PaymentEvent buildEvent(Payment p, boolean success) {
        return PaymentEvent.builder()
            .orderId(p.getOrderId())
            .paymentId(p.getId())
            .amount(p.getAmount())
            .success(success)
            .failureReason(p.getFailureReason())
            .build();
    }
}
