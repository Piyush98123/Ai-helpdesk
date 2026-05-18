package com.ai.helpdesk.repository;

import com.ai.helpdesk.entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, Long> {

    @Query("select u from Tickets u where u.username = :username")
    Optional<Tickets> findByUsername(String username);

}
