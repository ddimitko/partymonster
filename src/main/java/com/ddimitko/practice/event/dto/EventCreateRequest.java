package com.ddimitko.practice.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EventCreateRequest(
        @NotBlank String title,
        String description,
        @NotBlank String location,
        @NotNull OffsetDateTime startTime,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @Min(1) int capacity
) {
}
