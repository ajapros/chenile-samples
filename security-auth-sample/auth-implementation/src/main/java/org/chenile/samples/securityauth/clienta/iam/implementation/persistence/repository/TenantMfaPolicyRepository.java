package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository;

import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.TenantMfaPolicyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantMfaPolicyRepository extends JpaRepository<TenantMfaPolicyEntity, Long> {

    @Query("""
            select p from TenantMfaPolicyEntity p
            join fetch p.realm r
            where lower(r.realmKey) = lower(:realmKey)
            """)
    Optional<TenantMfaPolicyEntity> findByRealm(@Param("realmKey") String realmKey);
}
