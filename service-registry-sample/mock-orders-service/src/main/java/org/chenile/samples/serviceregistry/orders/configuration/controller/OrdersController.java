package org.chenile.samples.serviceregistry.orders.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.samples.serviceregistry.orders.model.OrderRequest;
import org.chenile.samples.serviceregistry.orders.model.OrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ChenileController(value = "mockOrdersService", serviceName = "_ordersService_",
        healthCheckerName = "ordersHealthChecker")
public class OrdersController extends ControllerSupport {
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<GenericResponse<OrderResponse>> getOrder(
            HttpServletRequest request,
            @PathVariable String orderId) {
        return process("getOrder", request, orderId);
    }

    @PostMapping("/orders")
    public ResponseEntity<GenericResponse<OrderResponse>> createOrder(
            HttpServletRequest request,
            @RequestBody OrderRequest orderRequest) {
        return process("createOrder", request, orderRequest);
    }
}
