package org.chenile.samples.securityauth.serviceb.context;

import org.chenile.security.auth.framework.security.IssuerTenantResolver;
import org.chenile.security.auth.framework.security.JwtClaimUtils;
import org.chenile.security.auth.framework.security.RequestSecurityContext;
import org.chenile.security.auth.framework.security.RequestSecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestSecurityContextFilter extends OncePerRequestFilter {

    private final RequestSecurityContextHolder contextHolder;

    public RequestSecurityContextFilter(RequestSecurityContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tenantId = JwtClaimUtils.firstNonBlank(
                    IssuerTenantResolver.tenantFromJwt(jwtAuthenticationToken.getToken()),
                    request.getHeader("x-tenant-id"),
                    "unknown");
            String userId = JwtClaimUtils.firstNonBlank(
                    jwtAuthenticationToken.getToken().getClaimAsString("user_id"),
                    jwtAuthenticationToken.getToken().getClaimAsString("preferred_username"),
                    jwtAuthenticationToken.getToken().getClaimAsString("azp"),
                    jwtAuthenticationToken.getToken().getSubject(),
                    request.getHeader("x-user-id"),
                    "unknown");
            var acls = JwtClaimUtils.extractAcls(jwtAuthenticationToken);
            contextHolder.set(new RequestSecurityContext(
                    userId,
                    tenantId,
                    acls,
                    null,
                    request.getHeader("x-user-id"),
                    request.getHeader("x-tenant-id"),
                    JwtClaimUtils.splitHeaderValues(request.getHeader("x-acls"))));
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            contextHolder.clear();
        }
    }
}
