package org.chenile.samples.securityauth.clienta.iam.implementation.persistence.repository;

import org.chenile.samples.securityauth.clienta.iam.implementation.persistence.entity.MfaChallengeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MfaChallengeRepository extends JpaRepository<MfaChallengeEntity, String> {

    @Query("""
            select c from MfaChallengeEntity c
            join fetch c.user u
            join fetch u.realm r
            where c.challengeId = :challengeId
            """)
    Optional<MfaChallengeEntity> findResolved(@Param("challengeId") String challengeId);
}
