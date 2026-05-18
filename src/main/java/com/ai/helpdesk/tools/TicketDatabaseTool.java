package com.ai.helpdesk.tools;

import com.ai.helpdesk.entity.Tickets;
import com.ai.helpdesk.enums.Priority;
import com.ai.helpdesk.enums.Status;
import com.ai.helpdesk.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TicketDatabaseTool {

    private TicketService ticketService;

    public TicketDatabaseTool(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // create ticket tool
    @Tool(description = "Creates a new support ticket in the database with user's issue details")
    public Tickets createTicketTool(
            @ToolParam(description = "Username or email of the user") String username,
            @ToolParam(description = "Brief summary of the issue") String summary,
            @ToolParam(description = "Detailed description of the problem") String description,
            @ToolParam(description = "Priority level: LOW, MEDIUM, HIGH, or URGENT") String priority,
            @ToolParam(description = "Category: HARDWARE, SOFTWARE, NETWORK, ACCESS, or OTHER") String category
    ) {
        log.info("Creating ticket for user: {}, summary: {}", username, summary);

        try {
            Tickets ticket = new Tickets();
            ticket.setUsername(username);
            ticket.setSummary(summary);
            ticket.setPriority(parsePriority(priority));
            ticket.setCategory(category);
            ticket.setStatus(Status.OPEN);

            // Don't set createdAt/updatedAt - @PrePersist handles it

            Tickets saved = ticketService.createTicket(ticket);
            log.info("Ticket created successfully with ID: {}", saved.getId());
            return saved;

        } catch (Exception e) {
            log.error("Error creating ticket: ", e);
            throw new RuntimeException("Failed to create ticket: " + e.getMessage());
        }
    }

    private Priority parsePriority(String priority) {
        if (priority == null) return Priority.MEDIUM;
        try {
            return Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Priority.MEDIUM;
        }
    }

    @Tool(description = "this tool helps to get ticket details by username")
    public Tickets getTicketByUserName(@ToolParam(description = "username whose ticket details needed") String username){
        return ticketService.getTicketByUserName(username);
    }

    @Tool(description = "Updates an existing ticket's status, priority, or other details")
    public Tickets updateTicketTool(
            @ToolParam(description = "ID of the ticket to update") Long ticketId,
            @ToolParam(description = "New status: OPEN, IN_PROGRESS, RESOLVED, CLOSED (optional)") String status,
            @ToolParam(description = "New priority: LOW, MEDIUM, HIGH, URGENT (optional)") String priority,
            @ToolParam(description = "Updated summary of the issue (optional)") String summary
    ) {
        log.info("Updating ticket ID: {} with status: {}, priority: {}", ticketId, status, priority);

        Tickets request = Tickets.builder()
                .id(ticketId)
                .status(status != null ? Status.valueOf(status.toUpperCase()) : null)
                .priority(priority != null ? Priority.valueOf(priority.toUpperCase()) : null)
                .summary(summary)
                .build();

        return ticketService.updateTicket(request);
    }

    @Tool(description = "this tool helps to get the current date time")
    public String getCurrentDateTime(){
        return String.valueOf(System.currentTimeMillis());
    }

//    @Tool(description = "this tool helps send the ticket details to support team")
//    public void sendEmailToSupportTeam(@ToolParam(description = "username of the user") String username){
//        System.out.println("Sending the details to support team "+username);
//    }



}
