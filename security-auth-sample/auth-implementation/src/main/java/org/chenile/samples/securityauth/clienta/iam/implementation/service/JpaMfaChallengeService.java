package org.chenile.samples.securityauth.clienta.iam.implementation.service;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import org.chenile.security.auth.framework.contract.MfaChallengeService;
import org.chenile.security.auth.framework.contract.MfaChallengeService.MfaChallenge;
import org.chenile.security.auth.framework.contract.MfaChallengeService.VerifiedMfaChallenge;
import org.chenile.security.auth.framework.contract.MfaPolicyService.MfaPolicy;
import org.chenile.security.auth.framework.contract.MfaProvider;
import org.chenile.security.auth.framework.contract.TenantRegistry.ResolvedUserProvider;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthProviderEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthUserEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.MfaChallengeEntity;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.AuthProviderRepository;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.AuthUserRepository;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository.MfaChallengeRepository;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class JpaMfaChallengeService implements MfaChallengeService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_VERIFIED = "VERIFIED";
    private static final String STATUS_FAILED = "FAILED";

    private final MfaChallengeRepository challengeRepository;
    private final AuthUserRepository userRepository;
    private final AuthProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final List<MfaProvider> mfaProviders;

    public JpaMfaChallengeService(
            MfaChallengeRepository challengeRepository,
            AuthUserRepository userRepository,
            AuthProviderRepository providerRepository,
            PasswordEncoder passwordEncoder,
            List<MfaProvider> mfaProviders) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.passwordEncoder = passwordEncoder;
        this.mfaProviders = mfaProviders;
    }

    @Override
    @Transactional
    public MfaChallenge start(
            ResolvedUserProvider primaryProvider,
            String clientId,
            AuthProviderType primaryProviderType,
            MfaPolicy policy) {
        AuthUserEntity user = userRepository.findActiveUser(primaryProvider.realm(), primaryProvider.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown MFA user"));
        Optional<MfaProvider> externalProvider = mfaProvider(policy.providerKey(), policy.providerType());
        if (externalProvider.isEmpty()) {
            providerRepository.findActiveProviderForUser(user.getId(), policy.providerKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MFA provider is not enabled for user"));
        }

        Instant expiresAt = Instant.now().plus(policy.challengeTtl());
        MfaChallengeEntity challenge = new MfaChallengeEntity();
        challenge.setChallengeId(UUID.randomUUID().toString());
        challenge.setUser(user);
        challenge.setPrimaryProviderId(primaryProvider.id());
        challenge.setClientId(clientId);
        challenge.setPrimaryProviderType(primaryProviderType);
        challenge.setMfaProviderKey(policy.providerKey());
        challenge.setMfaProviderType(policy.providerType());
        challenge.setStatus(STATUS_PENDING);
        challenge.setAttempts(0);
        challenge.setCreatedAt(Instant.now());
        challenge.setExpiresAt(expiresAt);
        challengeRepository.save(challenge);

        return new MfaChallenge(
                challenge.getChallengeId(),
                policy.providerKey(),
                policy.providerType(),
                policy.displayName(),
                externalProvider
                        .map(provider -> provider.destinationHint(primaryProvider, policy))
                        .orElse(policy.destinationHint()),
                expiresAt);
    }

    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public VerifiedMfaChallenge verify(String challengeId, String code) {
        MfaChallengeEntity challenge = challengeRepository.findResolved(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA challenge"));
        if (!STATUS_PENDING.equals(challenge.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MFA challenge is not pending");
        }
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            challenge.setStatus(STATUS_FAILED);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MFA challenge expired");
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        boolean verified = mfaProvider(challenge.getMfaProviderKey(), challenge.getMfaProviderType())
                .map(provider -> provider.verify(toResolvedUserProvider(challenge), toPolicy(challenge), code))
                .orElseGet(() -> {
                    AuthProviderEntity provider = providerRepository.findActiveProviderForUser(
                                    challenge.getUser().getId(),
                                    challenge.getMfaProviderKey())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MFA provider is not enabled for user"));
                    return passwordEncoder.matches(code, provider.getProviderSecret());
                });
        if (!verified) {
            if (challenge.getAttempts() >= 3) {
                challenge.setStatus(STATUS_FAILED);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA code");
        }
        challenge.setStatus(STATUS_VERIFIED);
        return new VerifiedMfaChallenge(
                challenge.getChallengeId(),
                challenge.getUser().getRealm().getRealmKey(),
                challenge.getUser().getEmail(),
                challenge.getPrimaryProviderId(),
                challenge.getClientId(),
                challenge.getPrimaryProviderType(),
                challenge.getMfaProviderKey(),
                challenge.getMfaProviderType());
    }

    private Optional<MfaProvider> mfaProvider(String providerKey, AuthProviderType providerType) {
        return mfaProviders.stream()
                .filter(provider -> provider.providerKey().equals(providerKey) && provider.providerType() == providerType)
                .findFirst();
    }

    private ResolvedUserProvider toResolvedUserProvider(MfaChallengeEntity challenge) {
        AuthUserEntity user = challenge.getUser();
        return new ResolvedUserProvider(
                challenge.getPrimaryProviderId(),
                user.getRealm().getRealmKey(),
                user.getRealm().getDisplayName(),
                user.getExternalId(),
                user.getUsername(),
                user.getEmail(),
                challenge.getMfaProviderKey(),
                challenge.getMfaProviderKey(),
                challenge.getPrimaryProviderType(),
                List.of());
    }

    private MfaPolicy toPolicy(MfaChallengeEntity challenge) {
        return new MfaPolicy(
                true,
                challenge.getMfaProviderKey(),
                challenge.getMfaProviderType(),
                challenge.getMfaProviderKey(),
                challenge.getMfaProviderKey(),
                Duration.between(Instant.now(), challenge.getExpiresAt()),
                List.of(challenge.getMfaProviderType()));
    }
}
