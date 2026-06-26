package org.chenile.samples.securityauth.serviceb.web;

import org.chenile.security.auth.framework.security.RequestSecurityContextHolder;
import org.chenile.security.auth.framework.security.TenantAccessPolicy;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/b/customers")
public class CustomerController {

    private static final List<Map<String, Object>> TENANT_ALPHA_CUSTOMERS = List.of(
            Map.of("customerId", "B-ALPHA-900", "tenantId", "tenant-alpha", "tier", "GOLD", "region", "IN"),
            Map.of("customerId", "B-ALPHA-901", "tenantId", "tenant-alpha", "tier", "SILVER", "region", "SG"));
    private static final List<Map<String, Object>> TENANT_BETA_CUSTOMERS = List.of(
            Map.of("customerId", "B-BETA-950", "tenantId", "tenant-beta", "tier", "PLATINUM", "region", "US"),
            Map.of("customerId", "B-BETA-951", "tenantId", "tenant-beta", "tier", "GOLD", "region", "CA"));
    private static final List<Map<String, Object>> DEFAULT_TENANT_CUSTOMERS = List.of(
            Map.of("customerId", "B-TENANT-990", "tenantId", "tenant-generic", "tier", "STANDARD", "region", "GLOBAL"));

    private final RequestSecurityContextHolder contextHolder;

    public CustomerController(RequestSecurityContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    @GetMapping("/portfolio")
    public Map<String, Object> portfolio(JwtAuthenticationToken authentication) {
        var context = contextHolder.getRequired();
        TenantAccessPolicy.assertRelayMatchesTenant(context);
        var jwt = authentication.getToken();
        String tenantId = TenantAccessPolicy.effectiveTenant(context);
        return Map.of(
                "service", "service-b",
                "tenantId", tenantId,
                "userId", context.userId(),
                "audience", jwt.getAudience(),
                "customers", customersForTenant(tenantId));
    }

    @GetMapping("/context")
    public Map<String, Object> context() {
        TenantAccessPolicy.assertRelayMatchesTenant(contextHolder.getRequired());
        return Map.of(
                "service", "service-b",
                "requestContext", contextHolder.getRequired().asMap());
    }

    private List<Map<String, Object>> customersForTenant(String tenantId) {
        return switch (tenantId) {
            case "tenant-alpha" -> TENANT_ALPHA_CUSTOMERS;
            case "tenant-beta" -> TENANT_BETA_CUSTOMERS;
            case "platform" -> Stream.concat(TENANT_ALPHA_CUSTOMERS.stream(), TENANT_BETA_CUSTOMERS.stream()).toList();
            default -> DEFAULT_TENANT_CUSTOMERS.stream()
                    .map(customer -> Map.<String, Object>of(
                            "customerId", tenantCustomerId(tenantId),
                            "tenantId", tenantId,
                            "tier", customer.get("tier"),
                            "region", customer.get("region")))
                    .toList();
        };
    }

    private String tenantCustomerId(String tenantId) {
        return "B-" + tenantId.toUpperCase().replace('-', '_') + "-990";
    }
}
