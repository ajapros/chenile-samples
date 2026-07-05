package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;

@Entity
@Table(name = "tenant_mfa_policy")
public class TenantMfaPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "realm_id", nullable = false)
    private AuthRealmEntity realm;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "provider_key", nullable = false)
    private String providerKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private AuthProviderType providerType;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "destination_hint")
    private String destinationHint;

    @Column(name = "ttl_seconds", nullable = false)
    private long ttlSeconds;

    public Long getId() {
        return id;
    }

    public AuthRealmEntity getRealm() {
        return realm;
    }

    public void setRealm(AuthRealmEntity realm) {
        this.realm = realm;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public AuthProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(AuthProviderType providerType) {
        this.providerType = providerType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDestinationHint() {
        return destinationHint;
    }

    public void setDestinationHint(String destinationHint) {
        this.destinationHint = destinationHint;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public Duration ttl() {
        return Duration.ofSeconds(ttlSeconds);
    }
}
