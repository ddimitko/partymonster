package com.ddimitko.practice.auth;

import com.ddimitko.practice.auth.dto.AuthResponse;
import com.ddimitko.practice.auth.dto.OtpRequest;
import com.ddimitko.practice.auth.dto.OtpSentResponse;
import com.ddimitko.practice.auth.dto.OtpVerificationRequest;
import com.ddimitko.practice.auth.dto.ProfileRequest;
import com.ddimitko.practice.auth.dto.UserProfileResponse;
import com.ddimitko.practice.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/request-otp")
    public OtpSentResponse requestOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestOtp(request.email());
        return new OtpSentResponse("OTP sent to email", otpService.ttl().toSeconds());
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return authService.verifyAndLogin(request.email(), request.code());
    }

    @PostMapping("/complete-profile")
    public UserProfileResponse completeProfile(@AuthenticationPrincipal AppUser user,
                                               @Valid @RequestBody ProfileRequest request) {
        AppUser updated = authService.completeProfile(user, request);
        return toProfileResponse(updated);
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return toProfileResponse(user);
    }

    private UserProfileResponse toProfileResponse(AppUser user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getDisplayName(),
                user.getPhone(),
                user.isProfileCompleted()
        );
    }
}
