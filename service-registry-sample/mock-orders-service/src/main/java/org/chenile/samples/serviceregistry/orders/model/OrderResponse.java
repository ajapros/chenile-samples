package org.chenile.samples.serviceregistry.orders.model;

import java.math.BigDecimal;

public class OrderResponse {
    public String orderId;
    public String customerId;
    public String status;
    public BigDecimal amount;

    public OrderResponse() {
    }

    public OrderResponse(String orderId, String customerId, String status, BigDecimal amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
        this.amount = amount;
    }
}
