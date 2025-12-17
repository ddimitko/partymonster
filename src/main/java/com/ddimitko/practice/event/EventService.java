package com.ddimitko.practice.event;

import com.ddimitko.practice.event.dto.EventCreateRequest;
import com.ddimitko.practice.event.dto.EventResponse;
import com.ddimitko.practice.event.dto.HostSummary;
import com.ddimitko.practice.event.dto.TicketPurchaseRequest;
import com.ddimitko.practice.event.dto.TicketPurchaseResponse;
import com.ddimitko.practice.user.AppUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

    private final PartyEventRepository partyEventRepository;
    private final TicketPurchaseRepository ticketPurchaseRepository;

    public EventService(PartyEventRepository partyEventRepository, TicketPurchaseRepository ticketPurchaseRepository) {
        this.partyEventRepository = partyEventRepository;
        this.ticketPurchaseRepository = ticketPurchaseRepository;
    }

    @Transactional
    public PartyEvent createEvent(AppUser host, EventCreateRequest request) {
        requireProfile(host, "host events");

        PartyEvent event = new PartyEvent();
        event.setHost(host);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setStartTime(request.startTime().toInstant());
        event.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        event.setCapacity(request.capacity());
        event.setTicketsSold(0);
        return partyEventRepository.save(event);
    }

    public List<EventResponse> listEvents() {
        return partyEventRepository.findAll(Sort.by(Sort.Direction.ASC, "startTime"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEvent(Long id) {
        PartyEvent event = partyEventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return toResponse(event);
    }

    @Transactional
    public TicketPurchase purchaseTickets(AppUser buyer, Long eventId, TicketPurchaseRequest request) {
        requireProfile(buyer, "buy tickets");
        if (request.quantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }

        PartyEvent event = partyEventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        int available = event.getAvailableTickets();
        if (available < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough tickets available");
        }

        event.setTicketsSold(event.getTicketsSold() + request.quantity());
        BigDecimal totalPrice = event.getPrice().multiply(BigDecimal.valueOf(request.quantity()));

        TicketPurchase purchase = new TicketPurchase();
        purchase.setEvent(event);
        purchase.setBuyer(buyer);
        purchase.setQuantity(request.quantity());
        purchase.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP));
        partyEventRepository.save(event);
        return ticketPurchaseRepository.save(purchase);
    }

    public TicketPurchaseResponse toPurchaseResponse(TicketPurchase purchase) {
        return new TicketPurchaseResponse(
                purchase.getId(),
                purchase.getEvent().getId(),
                purchase.getQuantity(),
                purchase.getTotalPrice(),
                purchase.getPurchasedAt());
    }

    public EventResponse toResponse(PartyEvent event) {
        HostSummary host = new HostSummary(event.getHost().getId(), resolveDisplayName(event.getHost()), event.getHost().getEmail());
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getStartTime(),
                event.getPrice(),
                event.getCapacity(),
                event.getTicketsSold(),
                event.getAvailableTickets(),
                host);
    }

    private void requireProfile(AppUser user, String action) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        if (!user.isProfileCompleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete your profile to " + action);
        }
    }

    private String resolveDisplayName(AppUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getEmail();
    }
}
