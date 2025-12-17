package com.ddimitko.practice.auth;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByEmailIgnoreCaseAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, Instant now);
}
