package org.chenile.samples.serviceregistry.payments.model;

import java.math.BigDecimal;

public class PaymentRequest {
    public String orderId;
    public String paymentMethod;
    public BigDecimal amount;
}
