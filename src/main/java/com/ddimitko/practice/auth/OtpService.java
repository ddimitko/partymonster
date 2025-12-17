package com.ddimitko.practice.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(10);

    private final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final SecureRandom random = new SecureRandom();
    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public OtpService(OtpCodeRepository otpCodeRepository,
                      PasswordEncoder passwordEncoder,
                      JavaMailSender mailSender,
                      @Value("${app.mail.from:noreply@partyapp.local}") String fromAddress) {
        this.otpCodeRepository = otpCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        String code = generateCode();
        String hash = passwordEncoder.encode(code);
        Instant expiresAt = Instant.now().plus(OTP_TTL);

        otpCodeRepository.save(new OtpCode(normalizedEmail, hash, expiresAt));
        dispatchEmail(normalizedEmail, code);
    }

    @Transactional
    public boolean verifyOtp(String email, String submittedCode) {
        String normalizedEmail = normalizeEmail(email);
        var otp = otpCodeRepository
                .findTopByEmailIgnoreCaseAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        normalizedEmail, Instant.now())
                .orElse(null);

        if (otp == null) {
            return false;
        }

        boolean matches = passwordEncoder.matches(submittedCode, otp.getCodeHash());
        if (matches) {
            otp.setUsed(true);
        }
        return matches;
    }

    public Duration ttl() {
        return OTP_TTL;
    }

    private void dispatchEmail(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromAddress);
            message.setSubject("Your login code");
            message.setText("Use this code to sign in: " + code + "\nIt expires in " + OTP_TTL.toMinutes() + " minutes.");
            mailSender.send(message);
            logger.info("Sent OTP email to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw e;
        }
    }

    private String generateCode() {
        int value = random.nextInt(1_000_000);
        return String.format(Locale.US, "%06d", value);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
