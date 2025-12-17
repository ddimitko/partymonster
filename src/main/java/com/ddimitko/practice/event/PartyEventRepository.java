package com.ddimitko.practice.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyEventRepository extends JpaRepository<PartyEvent, Long> {
}
