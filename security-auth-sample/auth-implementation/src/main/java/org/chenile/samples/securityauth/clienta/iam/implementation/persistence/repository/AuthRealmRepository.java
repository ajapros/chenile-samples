package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository;

import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.AuthRealmEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRealmRepository extends JpaRepository<AuthRealmEntity, Long> {

    Optional<AuthRealmEntity> findByRealmKeyIgnoreCaseAndEnabledTrue(String realmKey);

    boolean existsByRealmKeyIgnoreCaseAndEnabledTrue(String realmKey);
}
