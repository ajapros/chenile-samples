package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository;

import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.OAuthClientEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OAuthClientRepository extends JpaRepository<OAuthClientEntity, Long> {

    @Query("""
            select c from OAuthClientEntity c
            join fetch c.realm r
            where lower(r.realmKey) = lower(:realmKey)
              and c.clientId = :clientId
            """)
    Optional<OAuthClientEntity> findClient(
            @Param("realmKey") String realmKey,
            @Param("clientId") String clientId);
}
