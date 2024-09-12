package ottosulaoja.drsulxx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;
import ottosulaoja.drsulxx.service.AuthService;
import ottosulaoja.drsulxx.service.security.LoginAttemptService;
import ottosulaoja.drsulxx.service.security.PasswordExpirationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private PasswordExpirationService passwordExpirationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    /**
     * Handles user login requests.
     * @param credentials A map containing username and password.
     * @return ResponseEntity with login status and messages.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        logger.info("Login request for user: {}", username);

        Map<String, String> response = new HashMap<>();

        try {
            // Check if the account is locked
            if (loginAttemptService.isAccountLocked(username)) {
                logger.warn("Account is locked for user: {}", username);
                response.put("error", "ACCOUNT_LOCKED");
                response.put("message", "Your account is locked due to too many failed login attempts. Please contact support.");
                return ResponseEntity.status(403).body(response);
            }

            // Authenticate the user
            boolean isAuthenticated = authService.authenticateUser(username, password);
            if (isAuthenticated) {
                logger.info("Login successful for user: {}", username);

                // Reset failed login attempts
                loginAttemptService.loginSucceeded(username);

                // Fetch the user and user security for password expiration check
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                UserSecurity userSecurity = userSecurityRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("UserSecurity not found"));

                // Check if the password has expired
                if (passwordExpirationService.isPasswordExpired(userSecurity)) {
                    logger.warn("Password expired for user: {}", username);
                    response.put("error", "PASSWORD_EXPIRED");
                    response.put("message", "Your password has expired. Please reset your password.");
                    return ResponseEntity.status(403).body(response);
                }

                response.put("message", "Login successful");
                return ResponseEntity.ok(response);
            } else {
                // Handle failed login
                logger.warn("Login failed for user: {}", username);
                int failedAttempts = loginAttemptService.loginFailed(username);

                // Check if failed attempts exceed the threshold to show CAPTCHA
                if (failedAttempts >= loginAttemptService.getMaxFailedAttempts()) {
                    response.put("captchaRequired", "true"); // Indicate that CAPTCHA is now required
                }

                response.put("error", "LOGIN_FAILED");
                response.put("message", "Invalid username or password");
                response.put("failedAttempts", String.valueOf(failedAttempts));
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            logger.error("Error during login process for user: {}", username, e);
            response.put("error", "INTERNAL_SERVER_ERROR");
            response.put("message", "Internal server error");
            return ResponseEntity.status(500).body(response);
        }
    }
    
}