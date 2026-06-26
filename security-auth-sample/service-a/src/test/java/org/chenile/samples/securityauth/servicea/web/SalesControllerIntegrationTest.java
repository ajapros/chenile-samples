package org.chenile.samples.securityauth.servicea.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.chenile.samples.securityauth.servicea.service.ServiceBProxy;
import java.util.List;
import java.util.Map;

@SpringBootTest
class SalesControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void summaryRequiresServiceAScope() throws Exception {
        mockMvc.perform(get("/api/a/orders/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void summaryReturnsTenantAwarePayloadForAuthorizedJwt() throws Exception {
        mockMvc.perform(get("/api/a/orders/summary")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-alpha")
                                        .claim("user_id", "alice")
                                        .claim("aud", java.util.List.of("service-a")))
                                .authorities(() -> "SCOPE_service-a.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-alpha"))
                .andExpect(jsonPath("$.userId").value("alice"))
                .andExpect(jsonPath("$.service").value("service-a"))
                .andExpect(jsonPath("$.orders[0].tenantId").value("tenant-alpha"))
                .andExpect(jsonPath("$.orders[0].orderId").value("A-ALPHA-1001"));
    }

    @Test
    void summaryRejectsConflictingTenantRelayHeaders() throws Exception {
        mockMvc.perform(get("/api/a/orders/summary")
                        .header("x-tenant-id", "tenant-beta")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-alpha")
                                        .claim("user_id", "alice")
                                        .claim("aud", java.util.List.of("service-a")))
                                .authorities(() -> "SCOPE_service-a.read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void summaryRejectsTokenWithoutServiceAScope() throws Exception {
        mockMvc.perform(get("/api/a/orders/summary")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-alpha")
                                        .claim("user_id", "alice")
                                        .claim("aud", java.util.List.of("service-a")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void summaryReturnsCombinedOrdersForPlatformTenant() throws Exception {
        mockMvc.perform(get("/api/a/orders/summary")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/platform")
                                        .claim("user_id", "ops-admin")
                                        .claim("aud", java.util.List.of("service-a")))
                                .authorities(() -> "SCOPE_service-a.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("platform"))
                .andExpect(jsonPath("$.orders[0].orderId").value("A-ALPHA-1001"))
                .andExpect(jsonPath("$.orders[2].orderId").value("A-BETA-2001"));
    }

    @Test
    void secureBridgeReturnsDownstreamContextWhenAuthorized() throws Exception {
        mockMvc.perform(get("/api/a/orders/secure-bridge")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-alpha")
                                        .claim("user_id", "alice")
                                        .claim("acls", java.util.List.of("bridge:invoke", "orders:read"))
                                        .claim("aud", java.util.List.of("service-a")))
                                .authorities(() -> "SCOPE_service-a.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("service-a"))
                .andExpect(jsonPath("$.requestContext.userId").value("alice"))
                .andExpect(jsonPath("$.downstreamContext.service").value("service-b"))
                .andExpect(jsonPath("$.downstreamPortfolio.customers[0].customerId").value("B-ALPHA-900"));
    }

    @TestConfiguration
    static class StubServiceBProxyConfiguration {

        @Bean
        @Primary
        ServiceBProxy serviceBProxy() {
            return new ServiceBProxy(null, null) {
                @Override
                public Map<String, Object> fetchCustomerPortfolio() {
                    return Map.of(
                            "service", "service-b",
                            "customers", List.of(Map.of("customerId", "B-ALPHA-900", "tenantId", "tenant-alpha")));
                }

                @Override
                public Map<String, Object> fetchCustomerContext() {
                    return Map.of(
                            "service", "service-b",
                            "requestContext", Map.of(
                                    "tenantId", "tenant-alpha",
                                    "userId", "alice"));
                }
            };
        }
    }
}
