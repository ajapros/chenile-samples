package org.chenile.samples.securityauth.clienta.iam.implementation.service;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import org.chenile.security.auth.framework.contract.MfaPolicyService;
import org.chenile.security.auth.framework.contract.TenantRegistry.ResolvedUserProvider;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.TenantMfaPolicyEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.TenantMfaPolicyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JpaMfaPolicyService implements MfaPolicyService {

    private final TenantMfaPolicyRepository tenantMfaPolicyRepository;

    public JpaMfaPolicyService(TenantMfaPolicyRepository tenantMfaPolicyRepository) {
        this.tenantMfaPolicyRepository = tenantMfaPolicyRepository;
    }

    @Override
    public MfaPolicy evaluate(ResolvedUserProvider primaryProvider, String clientId, AuthProviderType primaryProviderType) {
        return tenantMfaPolicyRepository.findByRealm(primaryProvider.realm())
                .filter(TenantMfaPolicyEntity::isEnabled)
                .map(policy -> new MfaPolicy(
                        true,
                        policy.getProviderKey(),
                        policy.getProviderType(),
                        policy.getDisplayName(),
                        policy.getDestinationHint(),
                        policy.ttl(),
                        List.of(policy.getProviderType())))
                .orElse(MfaPolicy.notRequired());
    }
}
