package com.ddimitko.practice.auth.dto;

public record AuthResponse(
        String token,
        boolean needsProfile,
        boolean newUser,
        String email
) {
}
