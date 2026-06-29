package org.chenile.samples.serviceregistry.orders.service.impl;

import org.chenile.samples.serviceregistry.orders.model.OrderRequest;
import org.chenile.samples.serviceregistry.orders.model.OrderResponse;
import org.chenile.samples.serviceregistry.orders.service.OrdersService;

import java.math.BigDecimal;

public class OrdersServiceImpl implements OrdersService {
    @Override
    public OrderResponse getOrder(String orderId) {
        return new OrderResponse(orderId, "customer-100", "CONFIRMED", BigDecimal.valueOf(42.50));
    }

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        String customerId = orderRequest == null ? "unknown" : orderRequest.customerId;
        BigDecimal amount = orderRequest == null || orderRequest.amount == null
                ? BigDecimal.ZERO : orderRequest.amount;
        return new OrderResponse("order-sample-1001", customerId, "CREATED", amount);
    }
}
