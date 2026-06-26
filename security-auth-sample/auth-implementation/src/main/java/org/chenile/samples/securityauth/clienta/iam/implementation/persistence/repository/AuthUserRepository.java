package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository;

import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthUserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, Long> {

    @Query("""
            select distinct u from AuthUserEntity u
            join fetch u.realm r
            left join fetch u.acls a
            where lower(r.realmKey) = lower(:realmKey)
              and u.username = :username
              and u.enabled = true
            """)
    Optional<AuthUserEntity> findActiveUser(
            @Param("realmKey") String realmKey,
            @Param("username") String username);
}
