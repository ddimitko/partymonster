package com.ddimitko.practice.auth.dto;

public record OtpSentResponse(
        String message,
        long expiresInSeconds
) {
}
