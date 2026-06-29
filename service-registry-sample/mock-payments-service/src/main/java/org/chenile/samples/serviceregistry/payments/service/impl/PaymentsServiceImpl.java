package org.chenile.samples.serviceregistry.payments.service.impl;

import org.chenile.samples.serviceregistry.payments.model.PaymentRequest;
import org.chenile.samples.serviceregistry.payments.model.PaymentResponse;
import org.chenile.samples.serviceregistry.payments.service.PaymentsService;

import java.math.BigDecimal;

public class PaymentsServiceImpl implements PaymentsService {
    @Override
    public PaymentResponse getPayment(String paymentId) {
        return new PaymentResponse(paymentId, "order-sample-1001", "AUTHORIZED", BigDecimal.valueOf(42.50));
    }

    @Override
    public PaymentResponse authorizePayment(PaymentRequest paymentRequest) {
        String orderId = paymentRequest == null ? "unknown" : paymentRequest.orderId;
        BigDecimal amount = paymentRequest == null || paymentRequest.amount == null
                ? BigDecimal.ZERO : paymentRequest.amount;
        return new PaymentResponse("payment-sample-9001", orderId, "AUTHORIZED", amount);
    }
}
