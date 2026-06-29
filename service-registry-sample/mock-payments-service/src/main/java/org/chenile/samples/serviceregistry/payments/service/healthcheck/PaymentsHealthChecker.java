package org.chenile.samples.serviceregistry.payments.service.healthcheck;

import org.chenile.core.service.HealthCheckInfo;
import org.chenile.core.service.HealthChecker;

public class PaymentsHealthChecker implements HealthChecker {
    @Override
    public HealthCheckInfo healthCheck() {
        HealthCheckInfo healthCheckInfo = new HealthCheckInfo();
        healthCheckInfo.healthy = true;
        healthCheckInfo.statusCode = 0;
        healthCheckInfo.message = "Payments service is healthy";
        return healthCheckInfo;
    }
}
