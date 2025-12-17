package com.ddimitko.practice.event.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EventResponse(
        Long id,
        String title,
        String description,
        String location,
        Instant startTime,
        BigDecimal price,
        int capacity,
        int ticketsSold,
        int availableTickets,
        HostSummary host
) {
}
