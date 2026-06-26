package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository;

import org.chenile.security.auth.framework.contract.AuthProviderType;
import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthProviderConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthProviderConfigRepository extends JpaRepository<AuthProviderConfigEntity, Long> {

    @Query("""
            select c from AuthProviderConfigEntity c
            join fetch c.realm r
            where lower(r.realmKey) = lower(:realmKey)
              and c.providerKey = :providerKey
              and c.providerType = :providerType
              and c.enabled = true
            """)
    Optional<AuthProviderConfigEntity> findActiveConfig(
            @Param("realmKey") String realmKey,
            @Param("providerKey") String providerKey,
            @Param("providerType") AuthProviderType providerType);
}
