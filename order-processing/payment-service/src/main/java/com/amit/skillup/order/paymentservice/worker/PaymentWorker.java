package com.amit.skillup.order.paymentservice.worker;

import com.amit.skillup.order.common.event.PaymentEvent;
import com.amit.skillup.order.paymentservice.service.PaymentService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentWorker {

    private final PaymentService paymentService;

    /**
     * Processes payment.
     * Outputs: paymentSuccess=true/false, paymentId
     */
    @JobWorker(type = "process-payment")
    public void processPayment(JobClient client, ActivatedJob job) {
        var vars       = job.getVariablesAsMap();
        String orderId     = (String) vars.get("orderId");
        String customerId  = (String) vars.get("customerId");
        BigDecimal amount  = new BigDecimal(vars.get("amount").toString());

        log.info("Processing payment for order {}", orderId);
        PaymentEvent result = paymentService.processPayment(orderId, customerId, amount);

        client.newCompleteCommand(job.getKey())
            .variables(Map.of(
                "paymentSuccess", result.isSuccess(),
                "paymentId",      result.getPaymentId()
            ))
            .send()
            .join();
    }

    /**
     * Compensation handler — triggered automatically by Camunda when compensation is activated.
     * Bound to [Process Payment] task via Compensation Boundary Event.
     */
    @JobWorker(type = "refund-payment")
    public void refundPayment(JobClient client, ActivatedJob job) {
        String orderId = (String) job.getVariablesAsMap().get("orderId");
        log.info("Compensation: refunding payment for order {}", orderId);
        paymentService.refundPayment(orderId);

        client.newCompleteCommand(job.getKey()).send().join();
    }
}
