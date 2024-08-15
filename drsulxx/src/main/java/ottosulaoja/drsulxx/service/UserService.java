package ottosulaoja.drsulxx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final Validator validator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, Validator validator, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<Map<String, Object>> createUser(User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate user input
            validateUser(user);

            // Check for existing user with the same username or email
            if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
                logger.warn("User with username {} or email {} already exists.", user.getUsername(), user.getEmail());
                response.put("message", "User with the same username or email already exists.");
                response.put("emailSent", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Set default values for security fields
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setCredentialsNonExpired(true);
            user.setAccountNonLocked(true);

            // Set createdAt and updatedAt
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            // Hash the user's password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save the user to the database
            User savedUser = userRepository.save(user);

            // Send welcome email
            boolean emailSent = sendWelcomeEmail(savedUser);

            UserResponse userResponse = new UserResponse(savedUser, emailSent);
            response.put("user", userResponse);
            response.put("emailSent", emailSent);

            // Log and return appropriate response based on email sending result
            if (emailSent) {
                logger.info("User created and welcome email successfully sent.");
                response.put("message", "User created and welcome email sent.");
            } else {
                logger.warn("User created, but failed to send welcome email.");
                response.put("message", "User created but failed to send welcome email.");
            }
            return ResponseEntity.ok(response);
        } catch (ConstraintViolationException e) {
            logger.error("Validation error creating user", e);
            response.put("message", "Validation error: " + e.getMessage());
            response.put("emailSent", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            response.put("message", "Error creating user.");
            response.put("emailSent", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<List<Map<String, Object>>> createUsers(List<User> users) {
        List<Map<String, Object>> responseList = new ArrayList<>();
        try {
            Set<User> validatedUsers = new HashSet<>();
            for (User user : users) {
                validateUser(user);

                // Check for existing user with the same username or email
                if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
                    logger.warn("User with username {} or email {} already exists.", user.getUsername(), user.getEmail());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("user", user);
                    errorResponse.put("message", "User with the same username or email already exists.");
                    errorResponse.put("emailSent", false);
                    responseList.add(errorResponse);
                    continue; // Skip this user
                }

                // Set default values for security fields
                user.setEnabled(true);
                user.setAccountNonExpired(true);
                user.setCredentialsNonExpired(true);
                user.setAccountNonLocked(true);

                // Set createdAt and updatedAt
                LocalDateTime now = LocalDateTime.now();
                user.setCreatedAt(now);
                user.setUpdatedAt(now);

                // Hash the user's password
                user.setPassword(passwordEncoder.encode(user.getPassword()));

                validatedUsers.add(user);
            }

            // Save users to the database
            List<User> savedUsers = userRepository.saveAll(validatedUsers);

            // Send welcome emails and log results
            for (User user : savedUsers) {
                boolean emailSent = sendWelcomeEmail(user);

                UserResponse userResponse = new UserResponse(user, emailSent);
                Map<String, Object> userResponseMap = new HashMap<>();
                userResponseMap.put("user", userResponse);
                userResponseMap.put("emailSent", emailSent);
                responseList.add(userResponseMap);

                if (emailSent) {
                    logger.info("Welcome email successfully sent to {}.", maskEmail(user.getEmail()));
                } else {
                    logger.warn("Failed to send welcome email to {}.", maskEmail(user.getEmail()));
                }
            }

            return ResponseEntity.ok(responseList);
        } catch (ConstraintViolationException e) {
            logger.error("Validation error creating users", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Validation error: " + e.getMessage());
            errorResponse.put("emailSent", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(errorResponse));
        } catch (Exception e) {
            logger.error("Error creating users", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error creating users.");
            errorResponse.put("emailSent", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(errorResponse));
        }
    }

    /**
     * Validates the user object.
     * @param user The user to validate.
     * @throws ConstraintViolationException if validation fails.
     */
    private void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder("Validation failed: ");
            for (ConstraintViolation<User> violation : violations) {
                message.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
            }
            throw new ConstraintViolationException(message.toString(), violations);
        }
    }

    /**
     * Sends a welcome email to the given user.
     * @param user The user to whom the email will be sent.
     * @return true if the email was sent successfully, false otherwise.
     */
    private boolean sendWelcomeEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn("User email is null or empty. Cannot send email.");
            return false;
        }

        try {
            emailService.sendSimpleMessage(
                user.getEmail(),
                "Welcome to Our Service",
                "Dear " + user.getName() + " " + user.getFamilyName() + ",\n\nThank you for registering with us!"
            );
            return true; // Indicate success
        } catch (Exception e) {
            logger.error("Failed to send email to {}.", maskEmail(user.getEmail()), e); // Log the error with masked email
            return false; // Indicate failure
        }
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