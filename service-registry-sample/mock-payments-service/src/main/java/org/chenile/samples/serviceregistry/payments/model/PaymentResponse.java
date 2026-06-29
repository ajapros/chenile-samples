package org.chenile.samples.serviceregistry.payments.model;

import java.math.BigDecimal;

public class PaymentResponse {
    public String paymentId;
    public String orderId;
    public String status;
    public BigDecimal amount;

    public PaymentResponse() {
    }

    public PaymentResponse(String paymentId, String orderId, String status, BigDecimal amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }
}
