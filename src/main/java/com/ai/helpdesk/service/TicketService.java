package com.ai.helpdesk.service;

import com.ai.helpdesk.entity.Tickets;
import com.ai.helpdesk.repository.TicketRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public Tickets createTicket(Tickets tickets){
        log.info("saving data {}",tickets);
        tickets.setId(null);
        return ticketRepository.save(tickets);
    }

    public Tickets getTicket(Long ticketId){
        return ticketRepository.findById(ticketId).orElse(null);
    }

    public Tickets getTicketByUserName(String username){
        return ticketRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public Tickets updateTicket(Tickets ticket) {
        log.info("Updating ticket #{}", ticket.getId());
        return ticketRepository.save(ticket);
    }

}
