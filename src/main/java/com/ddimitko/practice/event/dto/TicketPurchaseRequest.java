package com.ddimitko.practice.event.dto;

import jakarta.validation.constraints.Min;

public record TicketPurchaseRequest(
        @Min(1) int quantity
) {
}
