package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository;

import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthProviderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthProviderRepository extends JpaRepository<AuthProviderEntity, Long> {

    @Query("""
            select p from AuthProviderEntity p
            join fetch p.user u
            join fetch u.realm r
            where lower(u.email) = lower(:email)
              and u.enabled = true
              and p.enabled = true
            order by p.providerOrder, p.providerLabel
            """)
    List<AuthProviderEntity> findActiveProvidersForEmail(@Param("email") String email);

    @Query("""
            select distinct p from AuthProviderEntity p
            join fetch p.user u
            join fetch u.realm r
            left join fetch u.acls a
            where p.id = :providerId
              and lower(u.email) = lower(:email)
              and u.enabled = true
              and p.enabled = true
            """)
    Optional<AuthProviderEntity> findResolvedProvider(
            @Param("providerId") long providerId,
            @Param("email") String email);
}
