package org.chenile.samples.securityauth.clienta.iam.implementation.service;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import org.chenile.security.auth.framework.contract.ExternalProviderService;
import org.chenile.security.auth.framework.contract.ExternalProviderService.ProviderConfigDefinition;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthProviderConfigEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.AuthProviderConfigRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JpaExternalProviderService implements ExternalProviderService {

    private final AuthProviderConfigRepository authProviderConfigRepository;

    public JpaExternalProviderService(AuthProviderConfigRepository authProviderConfigRepository) {
        this.authProviderConfigRepository = authProviderConfigRepository;
    }

    public ProviderConfigDefinition providerConfig(String realm, String providerKey, AuthProviderType providerType) {
        AuthProviderConfigEntity entity = authProviderConfigRepository.findActiveConfig(realm, providerKey, providerType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active provider config for realm " + realm + " and provider " + providerKey));
        return new ProviderConfigDefinition(
                entity.getRealm().getRealmKey(),
                entity.getProviderKey(),
                entity.getProviderType(),
                entity.getClientId(),
                entity.getClientSecret(),
                entity.getAuthorizationUri(),
                entity.getTokenUri(),
                entity.getUserInfoUri(),
                parseScopes(entity.getScopes()));
    }

    private List<String> parseScopes(String scopes) {
        if (scopes == null || scopes.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(scopes.split("[,\\s]+"))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .distinct()
                .toList();
    }
}
