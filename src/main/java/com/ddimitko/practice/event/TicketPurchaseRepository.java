package com.ddimitko.practice.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Long> {

    List<TicketPurchase> findByBuyerId(Long buyerId);
}
