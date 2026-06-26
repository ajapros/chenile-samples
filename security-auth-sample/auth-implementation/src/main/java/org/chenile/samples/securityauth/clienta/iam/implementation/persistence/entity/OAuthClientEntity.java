package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "oauth_client")
public class OAuthClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "realm_id", nullable = false)
    private AuthRealmEntity realm;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "client_credentials_enabled", nullable = false)
    private boolean clientCredentialsEnabled;

    @Column(name = "password_grant_enabled", nullable = false)
    private boolean passwordGrantEnabled;

    @Column(name = "allowed_scopes")
    private String allowedScopes;

    public Long getId() {
        return id;
    }

    public AuthRealmEntity getRealm() {
        return realm;
    }

    public void setRealm(AuthRealmEntity realm) {
        this.realm = realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isClientCredentialsEnabled() {
        return clientCredentialsEnabled;
    }

    public void setClientCredentialsEnabled(boolean clientCredentialsEnabled) {
        this.clientCredentialsEnabled = clientCredentialsEnabled;
    }

    public boolean isPasswordGrantEnabled() {
        return passwordGrantEnabled;
    }

    public void setPasswordGrantEnabled(boolean passwordGrantEnabled) {
        this.passwordGrantEnabled = passwordGrantEnabled;
    }

    public String getAllowedScopes() {
        return allowedScopes;
    }

    public void setAllowedScopes(String allowedScopes) {
        this.allowedScopes = allowedScopes;
    }
}
