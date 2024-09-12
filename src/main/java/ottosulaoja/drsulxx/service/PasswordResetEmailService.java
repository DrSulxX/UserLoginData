package ottosulaoja.drsulxx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetEmailService.class);
    
    private final JavaMailSender javaMailSender;

    @Autowired
    public PasswordResetEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Sends a password reset email with a reset code to the user.
     * @param to the recipient email address
     * @param resetCode the reset code to be included in the email
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendPasswordResetCodeEmail(String to, String resetCode) {
        String subject = "Your Password Reset Code";
        String text = "To reset your password, please use the following code: " + resetCode +
                      "\nThis code will expire in 20 minutes.";

        return sendEmail(to, subject, text);
    }

    /**
     * Sends an email using the provided details.
     * @param to the recipient email address
     * @param subject the subject of the email
     * @param text the body of the email
     * @return true if the email was sent successfully, false otherwise
     */
    private boolean sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            javaMailSender.send(message);
            logger.info("Password reset email successfully sent to {}", maskEmail(to));
            return true;
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}. Error: {}", maskEmail(to), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Masks the email address for privacy in logs.
     * @param email the email address to mask
     * @return the masked email address
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