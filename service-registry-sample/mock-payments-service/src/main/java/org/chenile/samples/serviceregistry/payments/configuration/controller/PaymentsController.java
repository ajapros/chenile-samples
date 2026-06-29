package org.chenile.samples.serviceregistry.payments.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.samples.serviceregistry.payments.model.PaymentRequest;
import org.chenile.samples.serviceregistry.payments.model.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ChenileController(value = "mockPaymentsService", serviceName = "_paymentsService_",
        healthCheckerName = "paymentsHealthChecker")
public class PaymentsController extends ControllerSupport {
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<GenericResponse<PaymentResponse>> getPayment(
            HttpServletRequest request,
            @PathVariable String paymentId) {
        return process("getPayment", request, paymentId);
    }

    @PostMapping("/payments")
    public ResponseEntity<GenericResponse<PaymentResponse>> authorizePayment(
            HttpServletRequest request,
            @RequestBody PaymentRequest paymentRequest) {
        return process("authorizePayment", request, paymentRequest);
    }
}
