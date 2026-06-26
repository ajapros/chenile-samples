package org.chenile.samples.securityauth.servicea.service;

import org.chenile.security.auth.framework.security.RequestSecurityContext;
import org.chenile.security.auth.framework.security.RequestSecurityContextHolder;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ServiceBProxy {

    private final RestClient restClient;
    private final RequestSecurityContextHolder contextHolder;

    public ServiceBProxy(RestClient serviceBRestClient, RequestSecurityContextHolder contextHolder) {
        this.restClient = serviceBRestClient;
        this.contextHolder = contextHolder;
    }

    public Map<String, Object> fetchCustomerPortfolio() {
        RequestSecurityContext context = contextHolder.getRequired();
        return restClient.get()
                .uri("/api/b/customers/portfolio")
                .header("Authorization", "Bearer " + context.bearerToken())
                .header("x-user-id", context.userId())
                .header("x-tenant-id", context.tenantId())
                .header("x-acls", String.join(",", context.acls()))
                .retrieve()
                .body(Map.class);
    }

    public Map<String, Object> fetchCustomerContext() {
        RequestSecurityContext context = contextHolder.getRequired();
        return restClient.get()
                .uri("/api/b/customers/context")
                .header("Authorization", "Bearer " + context.bearerToken())
                .header("x-user-id", context.userId())
                .header("x-tenant-id", context.tenantId())
                .header("x-acls", String.join(",", context.acls()))
                .retrieve()
                .body(Map.class);
    }
}
