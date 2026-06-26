package org.chenile.samples.securityauth.servicea.web;

import org.chenile.security.auth.framework.security.RequestSecurityContextHolder;
import org.chenile.security.auth.framework.security.TenantAccessPolicy;
import org.chenile.samples.securityauth.servicea.service.ServiceBProxy;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/a/orders")
public class SalesController {

    private static final List<Map<String, Object>> TENANT_ALPHA_ORDERS = List.of(
            Map.of("orderId", "A-ALPHA-1001", "tenantId", "tenant-alpha", "status", "APPROVED", "amount", 1500),
            Map.of("orderId", "A-ALPHA-1002", "tenantId", "tenant-alpha", "status", "PENDING", "amount", 700));
    private static final List<Map<String, Object>> TENANT_BETA_ORDERS = List.of(
            Map.of("orderId", "A-BETA-2001", "tenantId", "tenant-beta", "status", "APPROVED", "amount", 2100),
            Map.of("orderId", "A-BETA-2002", "tenantId", "tenant-beta", "status", "REVIEW", "amount", 350));
    private static final List<Map<String, Object>> DEFAULT_TENANT_ORDERS = List.of(
            Map.of("orderId", "A-TENANT-3001", "tenantId", "tenant-generic", "status", "APPROVED", "amount", 999));

    private final RequestSecurityContextHolder contextHolder;
    private final ServiceBProxy serviceBProxy;

    public SalesController(RequestSecurityContextHolder contextHolder, ServiceBProxy serviceBProxy) {
        this.contextHolder = contextHolder;
        this.serviceBProxy = serviceBProxy;
    }

    @GetMapping("/summary")
    public Map<String, Object> orderSummary(JwtAuthenticationToken authentication) {
        var context = contextHolder.getRequired();
        TenantAccessPolicy.assertRelayMatchesTenant(context);
        var jwt = authentication.getToken();
        String tenantId = TenantAccessPolicy.effectiveTenant(context);
        return Map.of(
                "service", "service-a",
                "tenantId", tenantId,
                "userId", context.userId(),
                "audience", jwt.getAudience(),
                "orders", ordersForTenant(tenantId));
    }

    @GetMapping("/secure-bridge")
    public Map<String, Object> secureBridge() {
        var context = contextHolder.getRequired();
        TenantAccessPolicy.assertRelayMatchesTenant(context);
        return Map.of(
                "service", "service-a",
                "requestContext", context.asMap(),
                "orders", ordersForTenant(TenantAccessPolicy.effectiveTenant(context)),
                "downstreamContext", serviceBProxy.fetchCustomerContext(),
                "downstreamPortfolio", serviceBProxy.fetchCustomerPortfolio());
    }

    private List<Map<String, Object>> ordersForTenant(String tenantId) {
        return switch (tenantId) {
            case "tenant-alpha" -> TENANT_ALPHA_ORDERS;
            case "tenant-beta" -> TENANT_BETA_ORDERS;
            case "platform" -> Stream.concat(TENANT_ALPHA_ORDERS.stream(), TENANT_BETA_ORDERS.stream()).toList();
            default -> DEFAULT_TENANT_ORDERS.stream()
                    .map(order -> Map.<String, Object>of(
                            "orderId", tenantOrderId(tenantId),
                            "tenantId", tenantId,
                            "status", order.get("status"),
                            "amount", order.get("amount")))
                    .toList();
        };
    }

    private String tenantOrderId(String tenantId) {
        return "A-" + tenantId.toUpperCase().replace('-', '_') + "-3001";
    }
}
