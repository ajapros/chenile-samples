package org.chenile.samples.serviceregistry.orders.configuration;

import org.chenile.samples.serviceregistry.orders.service.OrdersService;
import org.chenile.samples.serviceregistry.orders.service.healthcheck.OrdersHealthChecker;
import org.chenile.samples.serviceregistry.orders.service.impl.OrdersServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdersConfiguration {
    @Bean
    public OrdersService _ordersService_() {
        return new OrdersServiceImpl();
    }

    @Bean
    public OrdersHealthChecker ordersHealthChecker() {
        return new OrdersHealthChecker();
    }
}
