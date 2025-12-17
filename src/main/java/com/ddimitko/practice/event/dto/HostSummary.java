package com.ddimitko.practice.event.dto;

public record HostSummary(
        Long id,
        String displayName,
        String email
) {
}
