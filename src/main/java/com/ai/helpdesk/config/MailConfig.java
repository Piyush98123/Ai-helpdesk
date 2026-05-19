package com.ai.helpdesk.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Configuration
public class MailConfig {

    @Value("${helpdesk.from-email}")
    private String fromEmail;


    @Bean
    public EmailHelper emailHelper(JavaMailSender mailSender) {
        return new EmailHelper(mailSender, fromEmail);
    }

    public static class EmailHelper {

        private final JavaMailSender mailSender;
        private final String fromEmail;

        public EmailHelper(JavaMailSender mailSender, String fromEmail) {
            this.mailSender = mailSender;
            this.fromEmail = fromEmail;
        }

        public void send(String to, String subject, String htmlBody) throws MessagingException {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        }
    }
}

