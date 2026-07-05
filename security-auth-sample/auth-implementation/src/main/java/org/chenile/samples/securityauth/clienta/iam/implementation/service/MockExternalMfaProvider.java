package org.chenile.samples.securityauth.clienta.iam.implementation.service;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import org.chenile.security.auth.framework.contract.MfaPolicyService.MfaPolicy;
import org.chenile.security.auth.framework.contract.MfaProvider;
import org.chenile.security.auth.framework.contract.TenantRegistry.ResolvedUserProvider;
import org.springframework.stereotype.Component;

@Component
public class MockExternalMfaProvider implements MfaProvider {

    public String providerKey() {
        return "mock-external-mfa";
    }

    public AuthProviderType providerType() {
        return AuthProviderType.EXTERNAL;
    }

    public String destinationHint(ResolvedUserProvider primaryProvider, MfaPolicy policy) {
        return "Mock third-party MFA code for " + primaryProvider.email();
    }

    public boolean verify(ResolvedUserProvider primaryProvider, MfaPolicy policy, String code) {
        return "424242".equals(code);
    }
}
