package ottosulaoja.drsulxx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ottosulaoja.drsulxx.exception.DuplicateEntryException;
import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.service.UpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/users/update")
public class UpdateController {

    private final UpdateService updateService;
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);

    @Autowired
    public UpdateController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserById(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = updateService.updateUserById(id, user, new UserSecurity());  // Assuming default UserSecurity is needed
            if (updatedUser != null) {
                return new ResponseEntity<>(updatedUser, HttpStatus.OK);
            } else {
                logger.warn("User with ID {} not found.", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DuplicateEntryException e) {
            // Log only the error message, not the full stack trace
            logger.error("Duplicate entry detected for ID: {}", id);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Log full stack trace for other unexpected exceptions
            logger.error("Error updating user by ID: " + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/username/{username}")
    public ResponseEntity<?> updateUserByUsername(@PathVariable String username, @RequestBody User user) {
        try {
            List<User> updatedUsers = updateService.updateUserByUsername(username, user, new UserSecurity());
            if (!updatedUsers.isEmpty()) {
                return new ResponseEntity<>(updatedUsers, HttpStatus.OK);
            } else {
                logger.warn("User with username {} not found.", username);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DuplicateEntryException e) {
            // Log only the error message, not the full stack trace
            logger.error("Duplicate entry detected for username: {}", username);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Log full stack trace for other unexpected exceptions
            logger.error("Error updating user by username: " + username, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/email/{email}")
    public ResponseEntity<?> updateUserByEmail(@PathVariable String email, @RequestBody User user) {
        try {
            List<User> updatedUsers = updateService.updateUserByEmail(email, user, new UserSecurity());
            if (!updatedUsers.isEmpty()) {
                return new ResponseEntity<>(updatedUsers, HttpStatus.OK);
            } else {
                logger.warn("User with email {} not found.", email);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DuplicateEntryException e) {
            // Log only the error message, not the full stack trace
            logger.error("Duplicate entry detected for email: {}", email);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Log full stack trace for other unexpected exceptions
            logger.error("Error updating user by email: " + email, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}