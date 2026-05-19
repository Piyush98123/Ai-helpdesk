package com.ai.helpdesk.tools;

import com.ai.helpdesk.entity.Tickets;
import com.ai.helpdesk.enums.Priority;
import com.ai.helpdesk.enums.Status;
import com.ai.helpdesk.service.TicketEmailService;
import com.ai.helpdesk.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Component
@Slf4j
public class TicketDatabaseTool {

    private final TicketService ticketService;
    private final TicketEmailService ticketEmailService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public TicketDatabaseTool(TicketService ticketService,
                              TicketEmailService ticketEmailService) {
        this.ticketService = ticketService;
        this.ticketEmailService = ticketEmailService;
    }

    /**
     * Creates a ticket and immediately fires a confirmation email in the background.
     * The LLM collects all required fields before calling this tool (enforced by system prompt).
     */
    @Tool(description = """
            Creates a new support ticket in the database with the user's issue details.
            Call this ONLY after collecting: username, userEmail, summary, priority, and category.
            Returns a confirmation string with the ticket ID to share with the user.
            """)
    public String createTicketTool(
            @ToolParam(description = "Username or full name of the user") String username,
            @ToolParam(description = "User's email address — needed to send confirmation email") String userEmail,
            @ToolParam(description = "One-line summary of the issue") String summary,
            @ToolParam(description = "Priority level: LOW, MEDIUM, HIGH") String priority,
            @ToolParam(description = "Category: HARDWARE, SOFTWARE, NETWORK, ACCESS, or OTHER") String category
    ) {
        log.info("Creating ticket — user: {}, email: {}, priority: {}", username, userEmail, priority);

        try {
            if (userEmail == null || !userEmail.contains("@")) {
                return "ERROR: A valid email address is required to create a ticket and send confirmation. " +
                        "Please ask the user for their email.";
            }

            Tickets ticket = new Tickets();
            ticket.setUsername(username);
            ticket.setUserEmail(userEmail);
            ticket.setSummary(summary);
            ticket.setPriority(parsePriority(priority));
            ticket.setCategory(category);
            ticket.setStatus(Status.OPEN);

            Tickets saved = ticketService.createTicket(ticket);
            log.info("Ticket #{} created for {}", saved.getId(), username);

            // Fire confirmation email to user (async — does not block this response)
            ticketEmailService.sendTicketCreatedConfirmation(saved, userEmail);

            // Fire alert to support team for HIGH/URGENT (async)
            if (saved.getPriority() == Priority.HIGH || saved.getPriority() == Priority.URGENT) {
                ticketEmailService.sendSupportTeamAlert(saved);
            }

            return ("Ticket created successfully!\n" +
                    "Ticket ID   : #" + saved.getId() + "\n" +
                    "Summary     : " + saved.getSummary() + "\n" +
                    "Priority    : " + saved.getPriority().name() + "\n" +
                    "Status      : " + saved.getStatus().name() + "\n" +
                    "Category    : " + saved.getCategory() + "\n" +
                    "Created At  : " + saved.getCreatedAt().format(FORMATTER) + "\n" +
                    "Confirmation email sent to: " + userEmail);

        } catch (Exception e) {
            log.error("Error creating ticket for {}: {}", username, e.getMessage(), e);
            return "ERROR: Failed to create ticket — " + e.getMessage() +
                    ". Please try again or ask the user to contact support directly.";
        }
    }

    // ─── Get Ticket by Username ────

    @Tool(description = """
            Fetches the most recent open ticket for a user by their username.
            Use this before creating a new ticket to check for duplicates.
            Returns ticket details as text, or a message if no ticket found.
            """)
    public String getTicketByUserName(
            @ToolParam(description = "Username of the user whose ticket details are needed") String username
    ) {
        log.info("Fetching ticket for username: {}", username);

        try {
            Tickets ticket = ticketService.getTicketByUserName(username);
            if (ticket == null) {
                return "No existing ticket found for user: " + username;
            }
            return formatTicket(ticket);
        } catch (Exception e) {
            log.error("Error fetching ticket for {}: {}", username, e.getMessage());
            return "ERROR: Could not fetch ticket for user " + username + " — " + e.getMessage();
        }
    }

    // ─── Update Ticket ───────

    @Tool(description = """
            Updates an existing ticket's status, priority, or summary.
            Only pass fields that need to change — others will remain unchanged.
            If the user's email is provided, sends them a status update notification.
            Returns updated ticket details or an error message.
            """)
    public String updateTicketTool(
            @ToolParam(description = "ID of the ticket to update") Long ticketId,
            @ToolParam(description = "New status: OPEN, IN_PROGRESS, RESOLVED, or CLOSED. Pass null to keep current.") String status,
            @ToolParam(description = "New priority: LOW, MEDIUM, HIGH. Pass null to keep current.") String priority,
            @ToolParam(description = "Updated summary text. Pass null to keep current.") String summary,
            @ToolParam(description = "User's email address to send update notification. Pass null to skip email.") String userEmail
    ) {
        log.info("Updating ticket #{} — status: {}, priority: {}", ticketId, status, priority);

        try {

            Tickets existing = ticketService.getTicket(ticketId);
            if (existing == null) {
                return "ERROR: Ticket #" + ticketId + " not found. " +
                        "Please verify the ticket ID and try again.";
            }

            // Only update provided fields — leave others untouched
            if (status != null && !status.isBlank()) {
                existing.setStatus(parseStatus(status));
            }
            if (priority != null && !priority.isBlank()) {
                existing.setPriority(parsePriority(priority));
            }
            if (summary != null && !summary.isBlank()) {
                existing.setSummary(summary);
            }

            Tickets updated = ticketService.updateTicket(existing);
            log.info("Ticket #{} updated successfully", ticketId);

            // Send status update email if email was provided (async)
            if (userEmail != null && userEmail.contains("@")) {
                ticketEmailService.sendStatusUpdateNotification(updated, userEmail);
            }

            return ("Ticket #" + updated.getId() + " updated successfully!\n" +
                    "Status   : " + updated.getStatus().name() + "\n" +
                    "Priority : " + updated.getPriority().name() + "\n" +
                    "Summary  : " + updated.getSummary());

        } catch (IllegalArgumentException e) {
            return "ERROR: Invalid status or priority value — " + e.getMessage() +
                    ". Valid status: OPEN, IN_PROGRESS, RESOLVED, CLOSED. " +
                    "Valid priority: LOW, MEDIUM, HIGH, URGENT.";
        } catch (Exception e) {
            log.error("Error updating ticket #{}: {}", ticketId, e.getMessage(), e);
            return "ERROR: Failed to update ticket #" + ticketId + " — " + e.getMessage();
        }
    }


    /**
     * "can you resend my ticket confirmation?" without re-creating the ticket.
     */
    @Tool(description = """
            Resends a ticket confirmation email to the user.
            Use when a user says they didn't receive their confirmation email.
            Requires a valid ticket ID and the user's email address.
            """)
    public String resendConfirmationEmail(
            @ToolParam(description = "Ticket ID to resend confirmation for") Long ticketId,
            @ToolParam(description = "User's email address to send the confirmation to") String userEmail
    ) {
        log.info("Resending confirmation for ticket #{} to {}", ticketId, userEmail);

        try {
            if (userEmail == null || !userEmail.contains("@")) {
                return "ERROR: A valid email address is required.";
            }

            Tickets ticket = ticketService.getTicket(ticketId);
            if (ticket == null) {
                return "ERROR: Ticket #" + ticketId + " not found.";
            }

            ticketEmailService.sendTicketCreatedConfirmation(ticket, userEmail);
            return "Confirmation email for ticket #" + ticketId + " has been resent to " + userEmail + ".";

        } catch (Exception e) {
            log.error("Error resending email for ticket #{}: {}", ticketId, e.getMessage());
            return "ERROR: Could not resend email — " + e.getMessage();
        }
    }

    // ─── Get Current DateTime ────

    @Tool(description = "Returns the current date and time in a human-readable format.")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(FORMATTER);
    }


    private Priority parsePriority(String priority) {
        if (priority == null || priority.isBlank()) return Priority.MEDIUM;
        try {
            return Priority.valueOf(priority.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown priority '{}', defaulting to MEDIUM", priority);
            return Priority.MEDIUM;
        }
    }

    private Status parseStatus(String status) {
        try {
            return Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: '" + status + "'");
        }
    }

    private String formatTicket(Tickets t) {
        return ("Ticket #"   + t.getId()                    + "\n" +
                "Summary  : " + t.getSummary()              + "\n" +
                "Priority : " + t.getPriority().name()       + "\n" +
                "Status   : " + t.getStatus().name()          + "\n" +
                "Category : " + (t.getCategory() != null ? t.getCategory() : "N/A") + "\n" +
                "Created  : " + (t.getCreatedAt() != null ? t.getCreatedAt().format(FORMATTER) : "N/A"));
    }
}
