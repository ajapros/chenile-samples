package org.chenile.samples.serviceregistry.orders.service;

import org.chenile.samples.serviceregistry.orders.model.OrderRequest;
import org.chenile.samples.serviceregistry.orders.model.OrderResponse;

public interface OrdersService {
    OrderResponse getOrder(String orderId);

    OrderResponse createOrder(OrderRequest orderRequest);
}
