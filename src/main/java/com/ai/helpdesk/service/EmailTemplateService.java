package com.ai.helpdesk.service;

import com.ai.helpdesk.entity.Tickets;
import org.springframework.stereotype.Service;


@Service
public class EmailTemplateService {

    public String ticketCreatedUserEmail(Tickets ticket) {
        return """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                  <div style="max-width:600px; margin:auto; border:1px solid #e0e0e0; border-radius:8px; overflow:hidden;">
                    <div style="background:#1a73e8; padding:20px;">
                      <h2 style="color:white; margin:0;">XYZ Technologies — Helpdesk</h2>
                    </div>
                    <div style="padding:24px;">
                      <p>Hi <strong>%s</strong>,</p>
                      <p>Your support ticket has been created successfully. Here are the details:</p>
                      <table style="width:100%%; border-collapse:collapse; margin:16px 0;">
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold; width:40%%">Ticket ID</td>
                          <td style="padding:10px; border:1px solid #ddd;">#%d</td>
                        </tr>
                        <tr>
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Summary</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Priority</td>
                          <td style="padding:10px; border:1px solid #ddd;">
                            <span style="background:%s; color:white; padding:3px 10px; border-radius:12px; font-size:13px;">%s</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Status</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Category</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Created At</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                      </table>
                      <p>Our support team will be in touch shortly. You can reply to this email to add more details.</p>
                      <p style="color:#666; font-size:13px;">— XYZ Technologies Helpdesk Team</p>
                    </div>
                  </div>
                </body></html>
                """.formatted(
                ticket.getUsername(),
                ticket.getId(),
                ticket.getSummary(),
                priorityColor(ticket.getPriority().name()),
                ticket.getPriority().name(),
                ticket.getStatus().name(),
                ticket.getCategory() != null ? ticket.getCategory() : "General",
                ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : "Just now"
        );
    }


    public String ticketAlertSupportTeamEmail(Tickets ticket) {
        return """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                  <div style="max-width:600px; margin:auto; border:2px solid %s; border-radius:8px; overflow:hidden;">
                    <div style="background:%s; padding:16px;">
                      <h2 style="color:white; margin:0;">⚠️ New %s Priority Ticket — Action Required</h2>
                    </div>
                    <div style="padding:24px;">
                      <p>A new <strong>%s priority</strong> ticket has been raised and requires prompt attention.</p>
                      <table style="width:100%%; border-collapse:collapse; margin:16px 0;">
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold; width:40%%">Ticket ID</td>
                          <td style="padding:10px; border:1px solid #ddd;">#%d</td>
                        </tr>
                        <tr>
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">User</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Summary</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Category</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Created At</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                      </table>
                      <p style="color:#666; font-size:13px;">Please log in to the helpdesk system to assign and begin work on this ticket.</p>
                    </div>
                  </div>
                </body></html>
                """.formatted(
                priorityColor(ticket.getPriority().name()),
                priorityColor(ticket.getPriority().name()),
                ticket.getPriority().name(),
                ticket.getPriority().name(),
                ticket.getId(),
                ticket.getUsername(),
                ticket.getSummary(),
                ticket.getCategory() != null ? ticket.getCategory() : "General",
                ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : "Just now"
        );
    }

    public String ticketStatusUpdatedEmail(Tickets ticket, String userEmail) {
        return """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                  <div style="max-width:600px; margin:auto; border:1px solid #e0e0e0; border-radius:8px; overflow:hidden;">
                    <div style="background:#34a853; padding:20px;">
                      <h2 style="color:white; margin:0;">Ticket Update — XYZ Helpdesk</h2>
                    </div>
                    <div style="padding:24px;">
                      <p>Hi <strong>%s</strong>,</p>
                      <p>Your ticket <strong>#%d</strong> has been updated:</p>
                      <table style="width:100%%; border-collapse:collapse; margin:16px 0;">
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold; width:40%%">Ticket ID</td>
                          <td style="padding:10px; border:1px solid #ddd;">#%d</td>
                        </tr>
                        <tr>
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">New Status</td>
                          <td style="padding:10px; border:1px solid #ddd;"><strong>%s</strong></td>
                        </tr>
                        <tr style="background:#f5f5f5;">
                          <td style="padding:10px; border:1px solid #ddd; font-weight:bold;">Summary</td>
                          <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        </tr>
                      </table>
                      <p>If you have any questions, simply reply to this email.</p>
                      <p style="color:#666; font-size:13px;">— XYZ Technologies Helpdesk Team</p>
                    </div>
                  </div>
                </body></html>
                """.formatted(
                ticket.getUsername(),
                ticket.getId(),
                ticket.getId(),
                ticket.getStatus() != null ? ticket.getStatus().name() : "UPDATED",
                ticket.getSummary()
        );
    }

    // ─── Helpers ───

    private String priorityColor(String priority) {
        return switch (priority.toUpperCase()) {
            case "URGENT" -> "#d93025";  // red
            case "HIGH"   -> "#f29900";  // orange
            case "MEDIUM" -> "#1a73e8";  // blue
            default       -> "#5f6368";  // grey (LOW)
        };
    }
}

