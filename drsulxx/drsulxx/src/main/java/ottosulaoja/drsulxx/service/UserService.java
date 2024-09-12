package ottosulaoja.drsulxx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final Validator validator;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserSecurityRepository userSecurityRepository,
                       Validator validator, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSecurityRepository = userSecurityRepository;
        this.validator = validator;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "securityTransactionManager")
    public ResponseEntity<Map<String, Object>> createUser(User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate user input
            validateUser(user);

            // Check for existing user with the same username or email
            if (userRepository.existsByUsernameIgnoreCase(user.getUsername()) ||
                userRepository.existsByEmailIgnoreCase(user.getEmail())) {
                logger.warn("User with username {} or email {} already exists.", user.getUsername(), user.getEmail());
                response.put("message", "User with the same username or email already exists.");
                response.put("emailSent", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Set createdAt and updatedAt
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            // Hash the user's password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save the user to the database
            User savedUser = userRepository.save(user);
            logger.info("User saved with ID: {}", savedUser.getId());

            // Create and save UserSecurity without direct reference to the User entity
            UserSecurity userSecurity = new UserSecurity();
            userSecurity.setUserId(savedUser.getId());
            userSecurity.setEnabled(true);
            userSecurity.setAccountNonExpired(true);
            userSecurity.setCredentialsNonExpired(true);
            userSecurity.setAccountNonLocked(true);
            userSecurity.setPasswordLastChanged(now);
            userSecurityRepository.save(userSecurity);
            logger.info("UserSecurity saved for User ID: {}", savedUser.getId());

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
        } catch (DataAccessException e) {
            logger.error("Database error creating user", e); // Log specific database errors
            response.put("message", "Database error creating user.");
            response.put("emailSent", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            response.put("message", "Error creating user.");
            response.put("emailSent", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "securityTransactionManager")
    public ResponseEntity<List<Map<String, Object>>> createUsers(List<User> users) {
        List<Map<String, Object>> responseList = new ArrayList<>();
        Set<String> existingUsernames = new HashSet<>();
        Set<String> existingEmails = new HashSet<>();

        try {
            // Batch check existing usernames and emails
            for (User user : users) {
                if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
                    existingUsernames.add(user.getUsername().toLowerCase());
                }
                if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
                    existingEmails.add(user.getEmail().toLowerCase());
                }
            }

            Set<User> validatedUsers = new HashSet<>();
            List<UserSecurity> userSecurities = new ArrayList<>();

            for (User user : users) {
                validateUser(user);

                // Check for existing user with the same username or email
                if (existingUsernames.contains(user.getUsername().toLowerCase()) ||
                    existingEmails.contains(user.getEmail().toLowerCase())) {
                    logger.warn("User with username {} or email {} already exists.", user.getUsername(), user.getEmail());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("user", user);
                    errorResponse.put("message", "User with the same username or email already exists.");
                    errorResponse.put("emailSent", false);
                    responseList.add(errorResponse);
                    continue; // Skip this user
                }

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
            logger.info("Saved users count: {}", savedUsers.size());

            // Create and save UserSecurity for each user without direct reference
            for (User savedUser : savedUsers) {
                UserSecurity userSecurity = new UserSecurity();
                userSecurity.setUserId(savedUser.getId());
                userSecurity.setEnabled(true);
                userSecurity.setAccountNonExpired(true);
                userSecurity.setCredentialsNonExpired(true);
                userSecurity.setAccountNonLocked(true);
                userSecurity.setPasswordLastChanged(LocalDateTime.now());
                userSecurities.add(userSecurity);
            }

            // Make sure UserSecurity entries are only saved after User entries are committed
            logger.info("Attempting to save UserSecurity entries.");
            userSecurityRepository.saveAll(userSecurities);
            logger.info("UserSecurity entries saved successfully.");

            // Send welcome emails after committing the transaction
            for (User savedUser : savedUsers) {
                boolean emailSent = sendWelcomeEmail(savedUser);
                Map<String, Object> userResponse = new HashMap<>();
                userResponse.put("user", new UserResponse(savedUser, emailSent));
                userResponse.put("emailSent", emailSent);
                responseList.add(userResponse);
            }

            logger.info("Users created and welcome emails sent.");
            return ResponseEntity.ok(responseList);
        } catch (ConstraintViolationException e) {
            logger.error("Validation error creating users", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Validation error: " + e.getMessage());
            responseList.add(errorResponse);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseList);
        } catch (DataAccessException e) {
            logger.error("Database error creating users", e); // Log specific database errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Database error creating users.");
            responseList.add(errorResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseList);
        } catch (Exception e) {
            logger.error("Error creating users", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error creating users.");
            responseList.add(errorResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseList);
        }
    }

    private void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private boolean sendWelcomeEmail(User user) {
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}", user.getEmail(), e);
            return false;
        }
    }

    // Other methods like updateUser, deleteUser, etc. can go here
}