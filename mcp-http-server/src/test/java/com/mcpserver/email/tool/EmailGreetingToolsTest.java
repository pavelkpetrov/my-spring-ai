package com.mcpserver.email.tool;

import com.mcpserver.email.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailGreetingTools.
 *
 * This test class validates the send_greeting_email MCP tool functionality.
 * Tests cover successful email sending as well as failure scenarios for
 * validation errors.
 *
 * Testing Strategy:
 * - Mock EmailService to isolate the tool logic
 * - Test validation logic for email and greeting text
 * - Verify proper error handling and response formatting
 */
@ExtendWith(MockitoExtension.class)
class EmailGreetingToolsTest {

    @Mock
    private EmailService emailService;

    private EmailGreetingTools emailGreetingTools;

    @BeforeEach
    void setUp() {
        emailGreetingTools = new EmailGreetingTools(emailService);
    }

    // =========================================================================
    // SUCCESSFUL TEST
    // =========================================================================

    @Test
    @DisplayName("sendGreetingEmail - Success: Valid email and greeting text sends successfully")
    void whenSendGreetingEmail_withValidParameters_shouldReturnSuccess() {
        // --- Arrange ---
        String validEmail = "john.doe@example.com";
        String validGreeting = "Hello! Welcome to our service. We're glad to have you!";

        // Mock email service to succeed (do nothing)
        doNothing().when(emailService).sendGreetingEmail(validEmail, validGreeting);

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(validEmail, validGreeting);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Result should contain 3 entries");

        assertTrue((Boolean) result.get("success"), "Success flag should be true");
        assertEquals("Greeting email sent successfully", result.get("message"),
                "Success message should match");
        assertEquals(validEmail, result.get("toEmail"), "Email address should be included in response");

        assertFalse(result.containsKey("error"), "Error key should not be present on success");

        // --- Verify ---
        verify(emailService, times(1)).sendGreetingEmail(validEmail, validGreeting);
    }

    // =========================================================================
    // FAILURE TESTS
    // =========================================================================

    @Test
    @DisplayName("sendGreetingEmail - Failure: Invalid email address format")
    void whenSendGreetingEmail_withInvalidEmail_shouldReturnFailure() {
        // --- Arrange ---
        String invalidEmail = "not-a-valid-email";
        String validGreeting = "Hello! This is a greeting message.";

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(invalidEmail, validGreeting);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Result should contain 3 entries");

        assertFalse((Boolean) result.get("success"), "Success flag should be false");
        assertTrue(result.get("message").toString().contains("Invalid parameters"),
                "Message should indicate invalid parameters");
        assertTrue(result.get("message").toString().contains("Invalid email address format"),
                "Message should indicate email format error");
        assertEquals("Invalid email address format: " + invalidEmail, result.get("error"),
                "Error should describe the email validation failure");

        // --- Verify ---
        // Email service should never be called due to validation failure
        verify(emailService, never()).sendGreetingEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("sendGreetingEmail - Failure: Empty greeting text")
    void whenSendGreetingEmail_withEmptyGreetingText_shouldReturnFailure() {
        // --- Arrange ---
        String validEmail = "recipient@example.com";
        String emptyGreeting = "   "; // Only whitespace

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(validEmail, emptyGreeting);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Result should contain 3 entries");

        assertFalse((Boolean) result.get("success"), "Success flag should be false");
        assertTrue(result.get("message").toString().contains("Invalid parameters"),
                "Message should indicate invalid parameters");
        assertTrue(result.get("message").toString().contains("Greeting text cannot be empty"),
                "Message should indicate greeting text is empty");
        assertEquals("Greeting text cannot be empty", result.get("error"),
                "Error should describe the greeting text validation failure");

        // --- Verify ---
        // Email service should never be called due to validation failure
        verify(emailService, never()).sendGreetingEmail(anyString(), anyString());
    }

    // =========================================================================
    // Additional Edge Case Tests
    // =========================================================================

    @Test
    @DisplayName("sendGreetingEmail - Edge case: Null email address")
    void whenSendGreetingEmail_withNullEmail_shouldReturnFailure() {
        // --- Arrange ---
        String nullEmail = null;
        String validGreeting = "Hello!";

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(nullEmail, validGreeting);

        // --- Assert ---
        assertFalse((Boolean) result.get("success"), "Success flag should be false");
        assertTrue(result.get("error").toString().contains("Email address cannot be empty"),
                "Error should indicate email is empty");

        // --- Verify ---
        verify(emailService, never()).sendGreetingEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("sendGreetingEmail - Edge case: Null greeting text")
    void whenSendGreetingEmail_withNullGreeting_shouldReturnFailure() {
        // --- Arrange ---
        String validEmail = "test@example.com";
        String nullGreeting = null;

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(validEmail, nullGreeting);

        // --- Assert ---
        assertFalse((Boolean) result.get("success"), "Success flag should be false");
        assertTrue(result.get("error").toString().contains("Greeting text cannot be empty"),
                "Error should indicate greeting text is empty");

        // --- Verify ---
        verify(emailService, never()).sendGreetingEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("sendGreetingEmail - Edge case: Greeting text exceeds maximum length")
    void whenSendGreetingEmail_withTooLongGreeting_shouldReturnFailure() {
        // --- Arrange ---
        String validEmail = "test@example.com";
        // Create a greeting text longer than 5000 characters
        String tooLongGreeting = "a".repeat(5001);

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(validEmail, tooLongGreeting);

        // --- Assert ---
        assertFalse((Boolean) result.get("success"), "Success flag should be false");
        assertTrue(result.get("error").toString().contains("Greeting text is too long"),
                "Error should indicate greeting text is too long");
        assertTrue(result.get("error").toString().contains("max 5000 characters"),
                "Error should mention maximum character limit");

        // --- Verify ---
        verify(emailService, never()).sendGreetingEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("sendGreetingEmail - Failure: Email service throws MailException")
    void whenSendGreetingEmail_andServiceThrowsMailException_shouldReturnFailure() {
        // --- Arrange ---
        String validEmail = "test@example.com";
        String validGreeting = "Hello!";

        // Mock email service to throw MailException
        MailException mailException = new MailSendException("SMTP server connection failed");
        doThrow(mailException).when(emailService).sendGreetingEmail(validEmail, validGreeting);

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(validEmail, validGreeting);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertFalse((Boolean) result.get("success"), "Success flag should be false");
        assertTrue(result.get("message").toString().contains("Failed to send greeting email"),
                "Message should indicate email sending failure");
        assertEquals("MailSendException", result.get("error"),
                "Error should contain exception class name");

        // --- Verify ---
        verify(emailService, times(1)).sendGreetingEmail(validEmail, validGreeting);
    }

    @Test
    @DisplayName("sendGreetingEmail - Success: Valid email with special characters")
    void whenSendGreetingEmail_withValidEmailContainingSpecialChars_shouldReturnSuccess() {
        // --- Arrange ---
        String validEmail = "john.doe+test@example-domain.co.uk";
        String validGreeting = "Hello!";

        doNothing().when(emailService).sendGreetingEmail(validEmail, validGreeting);

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(validEmail, validGreeting);

        // --- Assert ---
        assertTrue((Boolean) result.get("success"), "Success flag should be true");
        assertEquals(validEmail, result.get("toEmail"), "Email should match");

        // --- Verify ---
        verify(emailService, times(1)).sendGreetingEmail(validEmail, validGreeting);
    }

    @Test
    @DisplayName("sendGreetingEmail - Success: Greeting text at maximum allowed length")
    void whenSendGreetingEmail_withMaxLengthGreeting_shouldReturnSuccess() {
        // --- Arrange ---
        String validEmail = "test@example.com";
        // Create a greeting text exactly 5000 characters (at the limit)
        String maxLengthGreeting = "a".repeat(5000);

        doNothing().when(emailService).sendGreetingEmail(validEmail, maxLengthGreeting);

        // --- Act ---
        Map<String, Object> result = emailGreetingTools.sendGreetingEmail(validEmail, maxLengthGreeting);

        // --- Assert ---
        assertTrue((Boolean) result.get("success"), "Success flag should be true");
        assertEquals("Greeting email sent successfully", result.get("message"));

        // --- Verify ---
        verify(emailService, times(1)).sendGreetingEmail(validEmail, maxLengthGreeting);
    }
}