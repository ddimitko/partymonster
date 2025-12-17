package com.ddimitko.practice.auth;

import com.ddimitko.practice.auth.dto.AuthResponse;
import com.ddimitko.practice.auth.dto.ProfileRequest;
import com.ddimitko.practice.user.AppUser;
import com.ddimitko.practice.user.AppUserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final AppUserRepository appUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final OtpService otpService;

    public AuthService(AppUserRepository appUserRepository, AuthTokenRepository authTokenRepository, OtpService otpService) {
        this.appUserRepository = appUserRepository;
        this.authTokenRepository = authTokenRepository;
        this.otpService = otpService;
    }

    public void requestOtp(String email) {
        otpService.sendOtp(email);
    }

    @Transactional
    public AuthResponse verifyAndLogin(String email, String code) {
        if (!otpService.verifyOtp(email, code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired OTP");
        }

        String normalizedEmail = normalizeEmail(email);
        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        boolean newUser = false;
        if (user == null) {
            user = new AppUser(normalizedEmail);
            user.setProfileCompleted(false);
            newUser = true;
            user = appUserRepository.save(user);
            logger.info("Created shell account for {}", normalizedEmail);
        }

        authTokenRepository.revokeAllForUser(user.getId());
        AuthToken token = new AuthToken(user, Instant.now().plus(TOKEN_TTL));
        authTokenRepository.save(token);

        return new AuthResponse(token.getToken(), !user.isProfileCompleted(), newUser, user.getEmail());
    }

    @Transactional
    public AppUser completeProfile(AppUser user, ProfileRequest request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
        }

        user.setFullName(request.fullName());
        user.setDisplayName(request.displayName());
        user.setPhone(request.phone());
        user.setProfileCompleted(true);
        return appUserRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
