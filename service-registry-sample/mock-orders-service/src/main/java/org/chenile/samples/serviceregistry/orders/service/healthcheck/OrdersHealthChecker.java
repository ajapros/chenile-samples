package org.chenile.samples.serviceregistry.orders.service.healthcheck;

import org.chenile.core.service.HealthCheckInfo;
import org.chenile.core.service.HealthChecker;

public class OrdersHealthChecker implements HealthChecker {
    @Override
    public HealthCheckInfo healthCheck() {
        HealthCheckInfo healthCheckInfo = new HealthCheckInfo();
        healthCheckInfo.healthy = true;
        healthCheckInfo.statusCode = 0;
        healthCheckInfo.message = "Orders service is healthy";
        return healthCheckInfo;
    }
}
