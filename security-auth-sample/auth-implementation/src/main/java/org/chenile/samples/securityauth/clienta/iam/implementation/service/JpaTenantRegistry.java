package org.chenile.samples.securityauth.clienta.iam.implementation.service;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import org.chenile.security.auth.framework.contract.TenantRegistry;
import org.chenile.security.auth.framework.contract.TenantRegistry.AuthProviderDefinition;
import org.chenile.security.auth.framework.contract.TenantRegistry.ClientDefinition;
import org.chenile.security.auth.framework.contract.TenantRegistry.RealmDefinition;
import org.chenile.security.auth.framework.contract.TenantRegistry.ResolvedUserProvider;
import org.chenile.security.auth.framework.contract.TenantRegistry.UserDefinition;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthProviderEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthRealmEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthUserEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.OAuthClientEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.AuthProviderRepository;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.AuthRealmRepository;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.AuthUserRepository;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.OAuthClientRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JpaTenantRegistry implements TenantRegistry {

    private static final List<String> DEFAULT_BROWSER_SCOPES =
            List.of("gateway.access", "service-a.read", "service-b.read");

    private final AuthRealmRepository realmRepository;
    private final AuthUserRepository userRepository;
    private final AuthProviderRepository providerRepository;
    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public JpaTenantRegistry(
            AuthRealmRepository realmRepository,
            AuthUserRepository userRepository,
            AuthProviderRepository providerRepository,
            OAuthClientRepository clientRepository,
            PasswordEncoder passwordEncoder) {
        this.realmRepository = realmRepository;
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RealmDefinition realm(String tenant) {
        AuthRealmEntity realm = realmRepository.findByRealmKeyIgnoreCaseAndEnabledTrue(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Unknown realm " + tenant));
        return new RealmDefinition(realm.getId(), realm.getRealmKey(), realm.getDisplayName());
    }

    public boolean realmExists(String tenant) {
        return realmRepository.existsByRealmKeyIgnoreCaseAndEnabledTrue(tenant);
    }

    @Transactional
    public boolean createRealm(String tenant) {
        try {
            AuthRealmEntity realm = new AuthRealmEntity();
            realm.setRealmKey(tenant);
            realm.setDisplayName(displayName(tenant));
            realm.setEnabled(true);
            realmRepository.save(realm);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    @Transactional
    public void registerClient(String tenant, ClientDefinition client) {
        AuthRealmEntity realm = realmRepository.findByRealmKeyIgnoreCaseAndEnabledTrue(tenant)
                .orElseThrow(() -> new IllegalArgumentException("Unknown realm " + tenant));
        OAuthClientEntity entity = clientRepository.findClient(tenant, client.clientId())
                .orElseGet(OAuthClientEntity::new);
        entity.setRealm(realm);
        entity.setClientId(client.clientId());
        entity.setClientSecret(client.secret() == null || client.secret().isBlank()
                ? null
                : passwordEncoder.encode(client.secret()));
        entity.setClientCredentialsEnabled(client.clientCredentialsEnabled());
        entity.setPasswordGrantEnabled(client.passwordGrantEnabled());
        entity.setAllowedScopes(String.join(",", client.allowedScopes()));
        clientRepository.save(entity);
    }

    public ClientDefinition client(String tenant, String clientId) {
        OAuthClientEntity client = clientRepository.findClient(tenant, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown client " + clientId + " for realm " + tenant));
        return new ClientDefinition(
                client.getClientId(),
                client.getClientSecret(),
                client.isClientCredentialsEnabled(),
                client.isPasswordGrantEnabled(),
                parseCsv(client.getAllowedScopes()));
    }

    public UserDefinition user(String tenant, String username) {
        AuthUserEntity user = userRepository.findActiveUser(tenant, username)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user " + username + " for realm " + tenant));
        return toUserDefinition(user);
    }

    public boolean matchesUserPassword(UserDefinition user, String rawPassword) {
        return rawPassword != null && passwordEncoder.matches(rawPassword, user.passwordSecretHash());
    }

    public boolean matchesClientSecret(ClientDefinition client, String rawSecret) {
        if (client.secretHash() == null) {
            return true;
        }
        return rawSecret != null && passwordEncoder.matches(rawSecret, client.secretHash());
    }

    public List<String> allowedScopes(String tenant, String clientId, List<String> requestedScopes) {
        ClientDefinition client = client(tenant, clientId);
        if (client.allowedScopes().isEmpty()) {
            return requestedScopes.stream().distinct().toList();
        }
        return requestedScopes.stream()
                .filter(client.allowedScopes()::contains)
                .distinct()
                .toList();
    }

    public List<AuthProviderDefinition> providersForEmail(String email) {
        return providerRepository.findActiveProvidersForEmail(email).stream()
                .map(this::toAuthProviderDefinition)
                .toList();
    }

    public ResolvedUserProvider resolvedProvider(long providerId, String email) {
        AuthProviderEntity provider = providerRepository.findResolvedProvider(providerId, email)
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider " + providerId + " for " + email));
        return toResolvedProvider(provider);
    }

    public boolean authenticate(long providerId, String email, String secret) {
        AuthProviderEntity provider = providerRepository.findResolvedProvider(providerId, email)
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider " + providerId + " for " + email));
        return secret != null && passwordEncoder.matches(secret, provider.getProviderSecret());
    }

    public List<String> defaultBrowserScopes() {
        return DEFAULT_BROWSER_SCOPES;
    }

    public Map<String, Object> createRealmPayload(String tenant) {
        return new LinkedHashMap<>(Map.of("realm", tenant, "enabled", true));
    }

    private UserDefinition toUserDefinition(AuthUserEntity user) {
        return new UserDefinition(
                user.getId(),
                user.getRealm().getRealmKey(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordSecret(),
                user.getAcls().stream()
                        .map(acl -> acl.getAclValue())
                        .sorted()
                        .toList());
    }

    private AuthProviderDefinition toAuthProviderDefinition(AuthProviderEntity provider) {
        return new AuthProviderDefinition(
                provider.getId(),
                provider.getUser().getRealm().getRealmKey(),
                provider.getUser().getRealm().getDisplayName(),
                provider.getUser().getUsername(),
                provider.getUser().getEmail(),
                provider.getProviderKey(),
                provider.getProviderLabel(),
                provider.getProviderType(),
                provider.getProviderOrder());
    }

    private ResolvedUserProvider toResolvedProvider(AuthProviderEntity provider) {
        AuthUserEntity user = provider.getUser();
        return new ResolvedUserProvider(
                provider.getId(),
                user.getRealm().getRealmKey(),
                user.getRealm().getDisplayName(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                provider.getProviderKey(),
                provider.getProviderLabel(),
                provider.getProviderType(),
                user.getAcls().stream()
                        .map(acl -> acl.getAclValue())
                        .sorted()
                        .toList());
    }

    private String displayName(String tenant) {
        String[] segments = tenant.split("-");
        return java.util.Arrays.stream(segments)
                .filter(segment -> !segment.isBlank())
                .map(segment -> Character.toUpperCase(segment.charAt(0)) + segment.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse(tenant);
    }

    private List<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .toList();
    }
}
