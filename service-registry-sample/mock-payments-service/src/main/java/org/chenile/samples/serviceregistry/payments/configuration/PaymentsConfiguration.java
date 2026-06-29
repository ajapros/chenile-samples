package org.chenile.samples.serviceregistry.payments.configuration;

import org.chenile.samples.serviceregistry.payments.service.PaymentsService;
import org.chenile.samples.serviceregistry.payments.service.healthcheck.PaymentsHealthChecker;
import org.chenile.samples.serviceregistry.payments.service.impl.PaymentsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentsConfiguration {
    @Bean
    public PaymentsService _paymentsService_() {
        return new PaymentsServiceImpl();
    }

    @Bean
    public PaymentsHealthChecker paymentsHealthChecker() {
        return new PaymentsHealthChecker();
    }
}
