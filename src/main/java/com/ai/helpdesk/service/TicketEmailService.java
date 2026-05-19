package com.ai.helpdesk.service;

import com.ai.helpdesk.config.MailConfig.EmailHelper;
import com.ai.helpdesk.entity.Tickets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketEmailService {

    private final EmailHelper emailHelper;
    private final EmailTemplateService emailTemplateService;

    @Value("${helpdesk.support-email}")
    private String supportEmail;

    // ─── Send confirmation to user after ticket created ─────────
    @Async
    public void sendTicketCreatedConfirmation(Tickets ticket, String userEmail) {
        try {
            String subject = "✅ Ticket #" + ticket.getId() + " Created — XYZ Helpdesk";
            String body = emailTemplateService.ticketCreatedUserEmail(ticket);
            emailHelper.send(userEmail, subject, body);
            log.info("Ticket confirmation email sent to {} for ticket #{}", userEmail, ticket.getId());
        } catch (Exception e) {
            // Log but DON'T rethrow — a mail failure should never roll back a ticket
            log.error("Failed to send ticket confirmation email to {} for ticket #{}: {}",
                    userEmail, ticket.getId(), e.getMessage());
        }
    }

    // ─── Alert support team for HIGH / URGENT tickets ────────
    @Async
    public void sendSupportTeamAlert(Tickets ticket) {
        try {
            String subject = "⚠️ [" + ticket.getPriority().name() + "] New Ticket #"
                    + ticket.getId() + " — Needs Attention";
            String body = emailTemplateService.ticketAlertSupportTeamEmail(ticket);
            emailHelper.send(supportEmail, subject, body);
            log.info("Support team alert sent to {} for ticket #{} ({})",
                    supportEmail, ticket.getId(), ticket.getPriority());
        } catch (Exception e) {
            log.error("Failed to send support team alert for ticket #{}: {}",
                    ticket.getId(), e.getMessage());
        }
    }

    // ─── Notify user when ticket status changes ──────
    @Async
    public void sendStatusUpdateNotification(Tickets ticket, String userEmail) {
        try {
            String subject = "🔄 Ticket #" + ticket.getId() + " Status Updated — XYZ Helpdesk";
            String body = emailTemplateService.ticketStatusUpdatedEmail(ticket, userEmail);
            emailHelper.send(userEmail, subject, body);
            log.info("Status update email sent to {} for ticket #{} → {}",
                    userEmail, ticket.getId(), ticket.getStatus());
        } catch (Exception e) {
            log.error("Failed to send status update email to {} for ticket #{}: {}",
                    userEmail, ticket.getId(), e.getMessage());
        }
    }
}
