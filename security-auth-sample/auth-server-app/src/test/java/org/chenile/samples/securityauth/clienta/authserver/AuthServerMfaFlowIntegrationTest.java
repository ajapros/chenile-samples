package org.chenile.samples.securityauth.clienta.authserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Instant;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.MfaChallengeEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.MfaChallengeRepository;
import org.chenile.security.auth.server.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.chenile.security.auth.server.web.LoginFlowController;

@SpringBootTest(
        classes = ClientAAuthServerApplication.class,
        properties = {
                "chenile.security.issuer-base=http://localhost:9000",
                "chenile.security.auth-server.token.access-token-ttl-seconds=3456",
                "chenile.security.auth-server.token.audiences.gateway.access=gateway",
                "chenile.security.auth-server.token.audiences.service-a.read=service-a",
                "chenile.security.auth-server.token.audiences.service-b.read=service-b"
        })
@Testcontainers
class AuthServerMfaFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("auth_server")
            .withUsername("auth_user")
            .withPassword("auth_pass");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoginFlowController controller;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MfaChallengeRepository challengeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from mfa_challenge");
        resetTenantPolicies();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void liquibaseCreatesMfaPolicyAndChallengeTables() {
        Integer policyCount = jdbcTemplate.queryForObject(
                "select count(*) from tenant_mfa_policy",
                Integer.class);
        Integer challengeCount = jdbcTemplate.queryForObject(
                "select count(*) from mfa_challenge",
                Integer.class);

        assertThat(policyCount).isGreaterThanOrEqualTo(3);
        assertThat(challengeCount).isZero();
    }

    @Test
    void tenantAlphaPasswordRequiresMfaThenIssuesMfaToken() throws Exception {
        MvcResult primary = mockMvc.perform(post("/api/login/authenticate")
                        .contentType("application/json")
                        .content("""
                                {"email":"gaurav.bhardwaj@getvymo.com","providerId":"PROV-tenant-alpha-alice-local-password","credential":"Alpha#Pass1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextStep").value("mfa"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andReturn();

        String challengeId = value(primary, "challengeId");
        MfaChallengeEntity pending = challengeRepository.findResolved(challengeId).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo("PENDING");
        assertThat(pending.getAttempts()).isZero();
        assertThat(pending.getMfaProviderKey()).isEqualTo("email-otp");

        MvcResult verified = mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"246810"}
                                """.formatted(challengeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(3456))
                .andExpect(jsonPath("$.authentication.mfa").value(true))
                .andReturn();

        JWTClaimsSet claims = tokenService.verifiedClaims(value(verified, "accessToken"));
        assertThat(claims.getStringClaim("tenant")).isEqualTo("tenant-alpha");
        assertThat(claims.getStringClaim("user_id")).isEqualTo("USR-tenant-alpha-alice");
        assertThat(claims.getBooleanClaim("mfa")).isTrue();
        assertThat(claims.getStringListClaim("amr")).containsExactly("pwd", "otp");
        assertThat(tokenLifetimeSeconds(claims)).isEqualTo(3456);
        assertThat(challengeRepository.findResolved(challengeId).orElseThrow().getStatus()).isEqualTo("VERIFIED");
    }

    @Test
    void tenantBetaPasswordIssuesTokenWithoutMfa() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login/authenticate")
                        .contentType("application/json")
                        .content("""
                                {"email":"bob@tenant-beta.local","providerId":"PROV-tenant-beta-bob-local-password","credential":"Bravo#Pass2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").value(3456))
                .andExpect(jsonPath("$.authentication.mfa").value(false))
                .andReturn();

        JWTClaimsSet claims = tokenService.verifiedClaims(value(result, "accessToken"));
        assertThat(claims.getStringClaim("tenant")).isEqualTo("tenant-beta");
        assertThat(claims.getStringClaim("user_id")).isEqualTo("USR-tenant-beta-bob");
        assertThat(claims.getBooleanClaim("mfa")).isFalse();
        assertThat(tokenLifetimeSeconds(claims)).isEqualTo(3456);
        assertThat(challengeRepository.count()).isZero();
    }

    @Test
    void wrongMfaCodeDoesNotIssueToken() throws Exception {
        MvcResult primary = mockMvc.perform(post("/api/login/authenticate")
                        .contentType("application/json")
                        .content("""
                                {"email":"gaurav.bhardwaj@getvymo.com","providerId":"PROV-tenant-alpha-alice-local-password","credential":"Alpha#Pass1"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"000000"}
                                """.formatted(value(primary, "challengeId"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid MFA code"));
    }

    @Test
    void threeWrongMfaAttemptsMarksChallengeFailed() throws Exception {
        String challengeId = alphaChallengeId();

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/login/mfa/verify")
                            .contentType("application/json")
                            .content("""
                                    {"challengeId":"%s","code":"000000"}
                                    """.formatted(challengeId)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid MFA code"));
        }

        assertThat(challengeAttempts(challengeId)).isEqualTo(3);
        assertThat(challengeStatus(challengeId)).isEqualTo("FAILED");
    }

    @Test
    void verifiedChallengeCannotBeReused() throws Exception {
        String challengeId = alphaChallengeId();
        mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"246810"}
                                """.formatted(challengeId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"246810"}
                                """.formatted(challengeId)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("MFA challenge is not pending"));
    }

    @Test
    void expiredChallengeIsRejectedAndMarkedFailed() throws Exception {
        String challengeId = alphaChallengeId();
        MfaChallengeEntity challenge = challengeRepository.findResolved(challengeId).orElseThrow();
        challenge.setExpiresAt(Instant.now().minusSeconds(1));
        challengeRepository.saveAndFlush(challenge);

        mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"246810"}
                                """.formatted(challengeId)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("MFA challenge expired"));

        assertThat(challengeStatus(challengeId)).isEqualTo("FAILED");
    }

    @Test
    void missingDatabaseBackedMfaProviderRejectsPrimaryLogin() throws Exception {
        jdbcTemplate.update("""
                update tenant_mfa_policy p
                set provider_key = 'missing-otp',
                    provider_type = 'OTP',
                    display_name = 'Missing OTP',
                    destination_hint = 'Missing OTP'
                from auth_realm r
                where p.realm_id = r.id and r.realm_key = 'tenant-alpha'
                """);

        mockMvc.perform(post("/api/login/authenticate")
                        .contentType("application/json")
                        .content("""
                                {"email":"gaurav.bhardwaj@getvymo.com","providerId":"PROV-tenant-alpha-alice-local-password","credential":"Alpha#Pass1"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("MFA provider is not enabled for user"));
    }

    @Test
    void mockExternalMfaProviderCanBeSelectedByTenantPolicy() throws Exception {
        jdbcTemplate.update("""
                update tenant_mfa_policy p
                set provider_key = 'mock-external-mfa',
                    provider_type = 'EXTERNAL',
                    display_name = 'Mock External MFA',
                    destination_hint = 'Original DB hint'
                from auth_realm r
                where p.realm_id = r.id and r.realm_key = 'tenant-alpha'
                """);

        MvcResult primary = mockMvc.perform(post("/api/login/authenticate")
                        .contentType("application/json")
                        .content("""
                                {"email":"gaurav.bhardwaj@getvymo.com","providerId":"PROV-tenant-alpha-alice-local-password","credential":"Alpha#Pass1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextStep").value("mfa"))
                .andExpect(jsonPath("$.provider.providerKey").value("mock-external-mfa"))
                .andExpect(jsonPath("$.provider.providerType").value("EXTERNAL"))
                .andExpect(jsonPath("$.provider.destinationHint").value("Mock third-party MFA code for gaurav.bhardwaj@getvymo.com"))
                .andReturn();

        mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"000000"}
                                """.formatted(value(primary, "challengeId"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid MFA code"));

        MvcResult verified = mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"424242"}
                                """.formatted(value(primary, "challengeId"))))
                .andExpect(status().isOk())
                .andReturn();

        JWTClaimsSet claims = tokenService.verifiedClaims(value(verified, "accessToken"));
        assertThat(claims.getStringClaim("mfa_provider")).isEqualTo("mock-external-mfa");
        assertThat(claims.getStringClaim("mfa_provider_type")).isEqualTo("EXTERNAL");
        assertThat(claims.getStringListClaim("amr")).containsExactly("pwd", "external");
    }

    @Test
    void serviceMeReturnsMfaClaimsAfterVerification() throws Exception {
        MvcResult primary = mockMvc.perform(post("/api/login/authenticate")
                        .contentType("application/json")
                        .content("""
                                {"email":"gaurav.bhardwaj@getvymo.com","providerId":"PROV-tenant-alpha-alice-local-password","credential":"Alpha#Pass1"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult verified = mockMvc.perform(post("/api/login/mfa/verify")
                        .contentType("application/json")
                        .content("""
                                {"challengeId":"%s","code":"246810"}
                                """.formatted(value(primary, "challengeId"))))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/api/service/me")
                        .header("Authorization", "Bearer " + value(verified, "accessToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authentication.mfa").value(true))
                .andExpect(jsonPath("$.authentication.amr[0]").value("pwd"))
                .andExpect(jsonPath("$.authentication.amr[1]").value("otp"));
    }

    private String alphaChallengeId() throws Exception {
        MvcResult primary = mockMvc.perform(post("/api/login/authenticate")
                        .contentType("application/json")
                        .content("""
                                {"email":"gaurav.bhardwaj@getvymo.com","providerId":"PROV-tenant-alpha-alice-local-password","credential":"Alpha#Pass1"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        return value(primary, "challengeId");
    }

    private long tokenLifetimeSeconds(JWTClaimsSet claims) {
        return (claims.getExpirationTime().getTime() - claims.getIssueTime().getTime()) / 1000;
    }

    private void resetTenantPolicies() {
        jdbcTemplate.update("""
                update tenant_mfa_policy p
                set enabled = true,
                    provider_key = 'email-otp',
                    provider_type = 'OTP',
                    display_name = 'Email OTP',
                    destination_hint = 'Seeded OTP for Tenant Alpha',
                    ttl_seconds = 300
                from auth_realm r
                where p.realm_id = r.id and r.realm_key = 'tenant-alpha'
                """);
        jdbcTemplate.update("""
                update tenant_mfa_policy p
                set enabled = false,
                    provider_key = 'local-password',
                    provider_type = 'PASSWORD',
                    display_name = 'Disabled',
                    destination_hint = 'Tenant Beta does not require MFA',
                    ttl_seconds = 300
                from auth_realm r
                where p.realm_id = r.id and r.realm_key = 'tenant-beta'
                """);
    }

    private String challengeStatus(String challengeId) {
        return jdbcTemplate.queryForObject(
                "select status from mfa_challenge where challenge_id = ?",
                String.class,
                challengeId);
    }

    private int challengeAttempts(String challengeId) {
        Integer attempts = jdbcTemplate.queryForObject(
                "select attempts from mfa_challenge where challenge_id = ?",
                Integer.class,
                challengeId);
        return attempts == null ? 0 : attempts;
    }

    private String value(MvcResult result, String key) throws Exception {
        String body = result.getResponse().getContentAsString();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + key + "\":\"([^\"]+)\"")
                .matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("Missing JSON key " + key + " in " + body);
        }
        return matcher.group(1);
    }
}
