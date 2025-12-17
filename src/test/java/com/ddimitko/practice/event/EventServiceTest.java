package com.ddimitko.practice.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddimitko.practice.event.dto.EventCreateRequest;
import com.ddimitko.practice.event.dto.TicketPurchaseRequest;
import com.ddimitko.practice.user.AppUser;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private PartyEventRepository partyEventRepository;

    @Mock
    private TicketPurchaseRepository ticketPurchaseRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(partyEventRepository, ticketPurchaseRepository);
    }

    @Test
    void createEvent_requiresCompletedProfile() {
        AppUser host = new AppUser("host@example.com");
        host.setProfileCompleted(false);
        EventCreateRequest request = new EventCreateRequest(
                "Party",
                "Fun night",
                "123 Main St",
                OffsetDateTime.now(ZoneOffset.UTC),
                BigDecimal.valueOf(25.00),
                10);

        assertThrows(ResponseStatusException.class, () -> eventService.createEvent(host, request));
    }

    @Test
    void purchaseTickets_updatesCountsAndTotal() {
        AppUser buyer = new AppUser("buyer@example.com");
        buyer.setProfileCompleted(true);

        PartyEvent event = new PartyEvent();
        event.setHost(buyer);
        event.setCapacity(10);
        event.setTicketsSold(0);
        event.setPrice(BigDecimal.valueOf(15.50));

        when(partyEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketPurchaseRepository.save(any(TicketPurchase.class))).thenAnswer(invocation -> {
            TicketPurchase purchase = invocation.getArgument(0);
            return purchase;
        });

        TicketPurchase result = eventService.purchaseTickets(buyer, 1L, new TicketPurchaseRequest(2));

        assertEquals(2, event.getTicketsSold());
        assertEquals(BigDecimal.valueOf(31.00).setScale(2), result.getTotalPrice());
        verify(partyEventRepository).save(event);
    }

    @Test
    void purchaseTickets_rejectsWhenOverCapacity() {
        AppUser buyer = new AppUser("buyer@example.com");
        buyer.setProfileCompleted(true);

        PartyEvent event = new PartyEvent();
        event.setHost(buyer);
        event.setCapacity(1);
        event.setTicketsSold(1);
        event.setPrice(BigDecimal.valueOf(10));

        when(partyEventRepository.findById(5L)).thenReturn(Optional.of(event));

        assertThrows(ResponseStatusException.class,
                () -> eventService.purchaseTickets(buyer, 5L, new TicketPurchaseRequest(1)));
    }
}
