package ottosulaoja.drsulxx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Send a simple email message.
     * @param to The recipient email address.
     * @param subject The subject of the email.
     * @param text The body of the email.
     * @return true if the email was sent successfully, false otherwise.
     */
    public boolean sendSimpleMessage(String to, String subject, String text) {
        if (!validateEmailInputs(to, subject, text)) {
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            javaMailSender.send(message);
            logger.info("Email successfully sent to {}", maskEmail(to));
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email to {}. Error: {}", maskEmail(to), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send a welcome email to a user.
     * @param to The recipient email address.
     * @param name The name of the user.
     * @return true if the email was sent successfully, false otherwise.
     */
    public boolean sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Our Service!";
        String text = String.format("Hello %s,\n\nWelcome to our service! We're excited to have you on board.\n\nBest regards,\nThe Team", name);
        return sendSimpleMessage(to, subject, text);
    }

    /**
     * Validate email inputs.
     * @param to The recipient email address.
     * @param subject The subject of the email.
     * @param text The body of the email.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateEmailInputs(String to, String subject, String text) {
        if (to == null || to.isEmpty()) {
            logger.warn("Recipient email address is null or empty.");
            return false;
        }
        if (subject == null || subject.isEmpty()) {
            logger.warn("Email subject is null or empty.");
            return false;
        }
        if (text == null || text.isEmpty()) {
            logger.warn("Email body text is null or empty.");
            return false;
        }
        return true;
    }

    /**
     * Masks the email address for logging purposes.
     * @param email The email address to mask.
     * @return The masked email address.
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (localPart.length() > 2) {
            localPart = localPart.substring(0, 2) + "*****";
        }
        return localPart + domain;
    }
}