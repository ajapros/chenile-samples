package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mfa_challenge")
public class MfaChallengeEntity {

    @Id
    @Column(name = "challenge_id", nullable = false)
    private String challengeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUserEntity user;

    @Column(name = "primary_provider_external_id", nullable = false)
    private String primaryProviderId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_provider_type", nullable = false)
    private AuthProviderType primaryProviderType;

    @Column(name = "mfa_provider_key", nullable = false)
    private String mfaProviderKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_provider_type", nullable = false)
    private AuthProviderType mfaProviderType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public AuthUserEntity getUser() {
        return user;
    }

    public void setUser(AuthUserEntity user) {
        this.user = user;
    }

    public String getPrimaryProviderId() {
        return primaryProviderId;
    }

    public void setPrimaryProviderId(String primaryProviderId) {
        this.primaryProviderId = primaryProviderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public AuthProviderType getPrimaryProviderType() {
        return primaryProviderType;
    }

    public void setPrimaryProviderType(AuthProviderType primaryProviderType) {
        this.primaryProviderType = primaryProviderType;
    }

    public String getMfaProviderKey() {
        return mfaProviderKey;
    }

    public void setMfaProviderKey(String mfaProviderKey) {
        this.mfaProviderKey = mfaProviderKey;
    }

    public AuthProviderType getMfaProviderType() {
        return mfaProviderType;
    }

    public void setMfaProviderType(AuthProviderType mfaProviderType) {
        this.mfaProviderType = mfaProviderType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
