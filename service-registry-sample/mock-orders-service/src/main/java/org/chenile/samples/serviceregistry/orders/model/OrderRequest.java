package org.chenile.samples.serviceregistry.orders.model;

import java.math.BigDecimal;

public class OrderRequest {
    public String customerId;
    public String sku;
    public int quantity;
    public BigDecimal amount;
}
