package com.ddimitko.practice.auth.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        String displayName,
        String phone,
        boolean profileCompleted
) {
}
