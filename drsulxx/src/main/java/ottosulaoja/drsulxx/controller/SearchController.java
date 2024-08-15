package ottosulaoja.drsulxx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.service.UserSearchService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final UserSearchService userSearchService;

    @Autowired
    public SearchController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userSearchService.getAllUsers().stream()
                .map(this::omitPassword) // Remove password from response
                .collect(Collectors.toList());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userSearchService.getUserById(id);
            if (user != null) {
                return ResponseEntity.ok(omitPassword(user));
            } else {
                logger.info("User not found for ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("Error fetching user by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<User>> getUsersByEmail(@PathVariable String email) {
        try {
            List<User> users = userSearchService.getUsersByEmail(email).stream()
                .map(this::omitPassword) // Remove password from response
                .collect(Collectors.toList());
            return users.isEmpty() ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<List<User>> getUsersByUsername(@PathVariable String username) {
        try {
            List<User> users = userSearchService.searchUsersByUsername(username).stream()
                .map(this::omitPassword) // Remove password from response
                .collect(Collectors.toList());
            return users.isEmpty() ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/firstName/{firstName}")
    public ResponseEntity<List<User>> getUsersByFirstName(@PathVariable String firstName) {
        try {
            List<User> users = userSearchService.searchUsersByName(firstName).stream()
                .map(this::omitPassword) // Remove password from response
                .collect(Collectors.toList());
            return users.isEmpty() ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by first name: {}", firstName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/lastName/{lastName}")
    public ResponseEntity<List<User>> getUsersByLastName(@PathVariable String lastName) {
        try {
            List<User> users = userSearchService.searchUsersByFamilyName(lastName).stream()
                .map(this::omitPassword) // Remove password from response
                .collect(Collectors.toList());
            return users.isEmpty() ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users by last name: {}", lastName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/query/{query}")
    public ResponseEntity<List<User>> searchUsers(@PathVariable String query) {
        try {
            List<User> users = userSearchService.searchUsersByQuery(query).stream()
                .map(this::omitPassword) // Remove password from response
                .collect(Collectors.toList());
            return users.isEmpty() ? ResponseEntity.status(HttpStatus.NOT_FOUND).build() : ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error executing search query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Method to remove password from User object
    private User omitPassword(User user) {
        if (user != null) {
            user.setPassword(null); // Ensure password is not included
        }
        return user;
    }
}