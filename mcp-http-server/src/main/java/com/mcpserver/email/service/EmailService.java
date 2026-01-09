package com.mcpserver.email.service;

import com.mcpserver.email.config.EmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails using Spring Mail.
 *
 * Reference: https://www.baeldung.com/spring-email
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    /**
     * Send a greeting email to the specified recipient.
     *
     * @param toEmail     Destination email address
     * @param greetingText Greeting message to send
     * @throws MailException if email sending fails
     */
    public void sendGreetingEmail(String toEmail, String greetingText) {
        log.info("Sending greeting email to: {}", toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getAddress());
        message.setTo(toEmail);
        message.setSubject("Greeting from " + emailProperties.getName());
        message.setText(greetingText);

        try {
            mailSender.send(message);
            log.info("Successfully sent greeting email to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send greeting email to: {}", toEmail, e);
            throw e;
        }
    }
}