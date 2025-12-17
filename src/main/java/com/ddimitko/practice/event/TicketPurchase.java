package com.ddimitko.practice.event;

import com.ddimitko.practice.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ticket_purchases")
public class TicketPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private PartyEvent event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_id", nullable = false)
    private AppUser buyer;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, scale = 2, precision = 15)
    private BigDecimal totalPrice;

    private Instant purchasedAt;

    public Long getId() {
        return id;
    }

    public PartyEvent getEvent() {
        return event;
    }

    public void setEvent(PartyEvent event) {
        this.event = event;
    }

    public AppUser getBuyer() {
        return buyer;
    }

    public void setBuyer(AppUser buyer) {
        this.buyer = buyer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Instant getPurchasedAt() {
        return purchasedAt;
    }

    @PrePersist
    public void setPurchasedAt() {
        this.purchasedAt = Instant.now();
    }
}
