package com.ddimitko.practice.event.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketPurchaseResponse(
        Long purchaseId,
        Long eventId,
        int quantity,
        BigDecimal totalPrice,
        Instant purchasedAt
) {
}
