package com.ddimitko.practice.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(otpCodeRepository, passwordEncoder, mailSender, "noreply@test.local");
    }

    @Test
    void sendOtp_savesHashedCodeAndSendsEmail() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-code");
        ArgumentCaptor<OtpCode> otpCaptor = ArgumentCaptor.forClass(OtpCode.class);
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        otpService.sendOtp("User@Example.com");

        verify(otpCodeRepository).save(otpCaptor.capture());
        verify(mailSender).send(mailCaptor.capture());

        OtpCode saved = otpCaptor.getValue();
        Duration remaining = Duration.between(Instant.now(), saved.getExpiresAt());
        assertEquals("user@example.com", saved.getEmail());
        assertEquals("hashed-code", saved.getCodeHash());
        assertFalse(saved.isUsed());
        assertTrue(remaining.compareTo(Duration.ofMinutes(9)) >= 0);
        assertTrue(remaining.compareTo(Duration.ofMinutes(11)) <= 0);

        SimpleMailMessage message = mailCaptor.getValue();
        assertEquals("noreply@test.local", message.getFrom());
        assertEquals("user@example.com", message.getTo()[0]);
        assertTrue(message.getText().contains("Use this code to sign in"));
    }

    @Test
    void verifyOtp_marksCodeAsUsedWhenMatches() {
        OtpCode code = new OtpCode("user@example.com", "hashed", Instant.now().plus(Duration.ofMinutes(5)));
        when(otpCodeRepository.findTopByEmailIgnoreCaseAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                eq("user@example.com"), any(Instant.class)))
                .thenReturn(java.util.Optional.of(code));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);

        boolean ok = otpService.verifyOtp("user@example.com", "123456");

        assertTrue(ok);
        assertTrue(code.isUsed());
    }

    @Test
    void verifyOtp_returnsFalseWhenNoCode() {
        when(otpCodeRepository.findTopByEmailIgnoreCaseAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                eq("user@example.com"), any(Instant.class)))
                .thenReturn(java.util.Optional.empty());

        boolean ok = otpService.verifyOtp("user@example.com", "000000");

        assertFalse(ok);
    }
}
