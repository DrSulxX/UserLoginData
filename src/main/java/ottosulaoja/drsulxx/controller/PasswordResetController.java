package ottosulaoja.drsulxx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ottosulaoja.drsulxx.service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        logger.info("Password reset request received for email: {}", email);

        try {
            passwordResetService.processForgotPassword(email);
            return ResponseEntity.ok(Map.of("message", "Password reset code has been sent to your email."));
        } catch (RuntimeException e) {
            logger.error("Error processing forgot password request for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred. Please try again later."));
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyResetToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        logger.info("Password reset token verification request received");

        boolean isValid = passwordResetService.verifyResetToken(token);

        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "Reset token is valid. You can now reset your password."));
        } else {
            logger.warn("Invalid or expired reset token received for verification.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid or expired reset token."));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        logger.info("Password reset request received for token: {}", token);

        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
        } catch (RuntimeException e) {
            logger.error("Error during password reset for token: {}", token, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid token or token expired."));
        }
    }
}