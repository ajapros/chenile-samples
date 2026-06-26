package org.chenile.samples.securityauth.serviceb.config;

import org.chenile.security.auth.framework.audit.ResourceAuditService;
import org.chenile.security.auth.framework.security.RequestSecurityContextHolder;
import org.chenile.security.auth.framework.security.ResourceServerAuthenticationManagerFactory;
import org.chenile.samples.securityauth.serviceb.context.RequestSecurityContextFilter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    RequestSecurityContextHolder requestSecurityContextHolder() {
        return new RequestSecurityContextHolder();
    }

    @Bean
    RequestSecurityContextFilter requestSecurityContextFilter(RequestSecurityContextHolder contextHolder) {
        return new RequestSecurityContextFilter(contextHolder);
    }

    @Bean
    ResourceAuditService resourceAuditService(MeterRegistry meterRegistry) {
        return new ResourceAuditService(
                meterRegistry,
                "SERVICE_B_AUDIT",
                "service-b",
                "chenile.security.service_b.token.failure");
    }

    @Bean
    ResourceServerAuthenticationManagerFactory resourceServerAuthenticationManagerFactory() {
        return new ResourceServerAuthenticationManagerFactory();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ResourceAuditService auditService,
            JwtIssuerAuthenticationManagerResolver authenticationManagerResolver,
            RequestSecurityContextFilter requestSecurityContextFilter) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/b/customers/**").hasAuthority("SCOPE_service-b.read")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(authenticationManagerResolver))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> {
                            auditService.logTokenValidationFailure(request.getRequestURI(), ex.getMessage());
                            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            auditService.logTokenValidationFailure(request.getRequestURI(), ex.getMessage());
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden");
                        }))
                .addFilterAfter(requestSecurityContextFilter, BearerTokenAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver(
            @Value("${chenile.security.jwt.issuer-base:http://localhost:9000}") String issuerBase,
            @Value("${chenile.security.jwt.jwk-base-uri:http://localhost:9000}") String jwkBaseUri,
            ResourceServerAuthenticationManagerFactory authenticationManagerFactory) {
        return authenticationManagerFactory.createResolver(issuerBase, jwkBaseUri, "service-b");
    }
}
