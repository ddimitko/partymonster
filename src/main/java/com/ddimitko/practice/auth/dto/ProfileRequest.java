package com.ddimitko.practice.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileRequest(
        @NotBlank String fullName,
        String displayName,
        String phone
) {
}
