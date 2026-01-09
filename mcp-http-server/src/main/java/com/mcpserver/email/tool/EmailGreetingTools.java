package com.mcpserver.email.tool;

import com.mcpserver.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP Tools for sending greeting emails.
 *
 * This service provides MCP-compatible tool functions for sending greeting emails.
 * Uses Spring Mail to send emails via SMTP.
 *
 * Reference: https://www.baeldung.com/spring-email
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailGreetingTools {

    private final EmailService emailService;

    /**
     * MCP Tool: Send a greeting email
     *
     * @param toEmail      Destination email address
     * @param greetingText Greeting message to send
     * @return Map containing the result status
     */
    @Tool(
            name = "send_greeting_email",
            description = """
                    Send a personalized greeting email to a specified recipient.

                    This tool sends a greeting email using the configured mail server.
                    The email will include the provided greeting text as the message body
                    and will be sent from the configured sender address.

                    Use this tool when you need to:
                    - Send personalized greetings to users
                    - Deliver welcome messages
                    - Send celebration or announcement messages
                    - Communicate custom greetings via email

                    Parameters:
                    - toEmail: Destination email address (required, must be valid email format)
                    - greetingText: The greeting message to send (required, max 5000 characters)

                    Returns:
                    - success: Boolean indicating if email was sent successfully
                    - message: Status message describing the result
                    - toEmail: The recipient email address (on success)
                    - error: Error message if operation failed
                    """
    )
    public Map<String, Object> sendGreetingEmail(
            @ToolParam(description = "Destination email address", required = true)
            String toEmail,

            @ToolParam(description = "The greeting message to send", required = true)
            String greetingText
    ) {
        log.info("MCP Tool invoked: send_greeting_email to {} with text length: {}",
                toEmail, greetingText != null ? greetingText.length() : 0);

        try {
            // Validate parameters
            validateEmail(toEmail);
            validateGreetingText(greetingText);

            // Send the email
            emailService.sendGreetingEmail(toEmail, greetingText);

            // Return success response
            return Map.of(
                    "success", true,
                    "message", "Greeting email sent successfully",
                    "toEmail", toEmail
            );

        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for send_greeting_email: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "message", "Invalid parameters: " + e.getMessage(),
                    "error", e.getMessage()
            );
        } catch (Exception e) {
            log.error("Failed to send greeting email", e);
            return Map.of(
                    "success", false,
                    "message", "Failed to send greeting email: " + e.getMessage(),
                    "error", e.getClass().getSimpleName()
            );
        }
    }

    /**
     * Validate email address format.
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty");
        }
        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }
    }

    /**
     * Validate greeting text.
     */
    private void validateGreetingText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Greeting text cannot be empty");
        }
        if (text.length() > 5000) {
            throw new IllegalArgumentException("Greeting text is too long (max 5000 characters)");
        }
    }
}