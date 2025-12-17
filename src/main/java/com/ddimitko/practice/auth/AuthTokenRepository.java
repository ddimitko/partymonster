package com.ddimitko.practice.auth;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

    Optional<AuthToken> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Query("update AuthToken t set t.revoked = true where t.user.id = :userId")
    void revokeAllForUser(Long userId);

    void deleteByExpiresAtBefore(Instant instant);
}
