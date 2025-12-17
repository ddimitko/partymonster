package com.ddimitko.practice.event;

import com.ddimitko.practice.event.dto.EventCreateRequest;
import com.ddimitko.practice.event.dto.EventResponse;
import com.ddimitko.practice.event.dto.TicketPurchaseRequest;
import com.ddimitko.practice.event.dto.TicketPurchaseResponse;
import com.ddimitko.practice.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> listEvents() {
        return eventService.listEvents();
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @PostMapping
    public EventResponse createEvent(@AuthenticationPrincipal AppUser host,
                                     @Valid @RequestBody EventCreateRequest request) {
        return eventService.toResponse(eventService.createEvent(host, request));
    }

    @PostMapping("/{id}/tickets")
    public TicketPurchaseResponse buyTickets(@AuthenticationPrincipal AppUser buyer,
                                             @PathVariable Long id,
                                             @Valid @RequestBody TicketPurchaseRequest request) {
        return eventService.toPurchaseResponse(eventService.purchaseTickets(buyer, id, request));
    }
}
