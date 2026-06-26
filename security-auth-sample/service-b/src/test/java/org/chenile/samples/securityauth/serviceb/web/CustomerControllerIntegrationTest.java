package org.chenile.samples.securityauth.serviceb.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class CustomerControllerIntegrationTest {

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
    void portfolioRequiresServiceBScope() throws Exception {
        mockMvc.perform(get("/api/b/customers/portfolio"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void portfolioReturnsTenantAwarePayloadForAuthorizedJwt() throws Exception {
        mockMvc.perform(get("/api/b/customers/portfolio")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-beta")
                                        .claim("user_id", "bob")
                                        .claim("aud", java.util.List.of("service-b")))
                                .authorities(() -> "SCOPE_service-b.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenant-beta"))
                .andExpect(jsonPath("$.userId").value("bob"))
                .andExpect(jsonPath("$.service").value("service-b"))
                .andExpect(jsonPath("$.customers[0].tenantId").value("tenant-beta"))
                .andExpect(jsonPath("$.customers[0].customerId").value("B-BETA-950"));
    }

    @Test
    void portfolioRejectsConflictingTenantRelayHeaders() throws Exception {
        mockMvc.perform(get("/api/b/customers/portfolio")
                        .header("x-tenant-id", "tenant-alpha")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-beta")
                                        .claim("user_id", "bob")
                                        .claim("aud", java.util.List.of("service-b")))
                                .authorities(() -> "SCOPE_service-b.read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void portfolioRejectsTokenWithoutServiceBScope() throws Exception {
        mockMvc.perform(get("/api/b/customers/portfolio")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-beta")
                                        .claim("user_id", "bob")
                                        .claim("aud", java.util.List.of("service-b")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void contextReturnsRelayedHeadersAndClaims() throws Exception {
        mockMvc.perform(get("/api/b/customers/context")
                        .header("x-user-id", "bob")
                        .header("x-tenant-id", "tenant-beta")
                        .header("x-acls", "customers:read,portfolio:view")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/tenant-beta")
                                        .claim("user_id", "bob")
                                        .claim("acls", java.util.List.of("customers:read", "portfolio:view"))
                                        .claim("aud", java.util.List.of("service-b")))
                                .authorities(() -> "SCOPE_service-b.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("service-b"))
                .andExpect(jsonPath("$.requestContext.userId").value("bob"))
                .andExpect(jsonPath("$.requestContext.headerTenantId").value("tenant-beta"));
    }

    @Test
    void portfolioReturnsCombinedCustomersForPlatformTenant() throws Exception {
        mockMvc.perform(get("/api/b/customers/portfolio")
                        .with(jwt().jwt(jwt -> jwt
                                        .issuer("http://localhost:9000/realms/platform")
                                        .claim("user_id", "ops-admin")
                                        .claim("aud", java.util.List.of("service-b")))
                                .authorities(() -> "SCOPE_service-b.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("platform"))
                .andExpect(jsonPath("$.customers[0].customerId").value("B-ALPHA-900"))
                .andExpect(jsonPath("$.customers[2].customerId").value("B-BETA-950"));
    }
}
