package ottosulaoja.drsulxx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.service.UserService;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        logger.debug("POST /api/users called with user: {}", user);
        try {
            ResponseEntity<Map<String, Object>> response = userService.createUser(user);
            logger.info("User creation response: {}", response);
            return response;
        } catch (ConstraintViolationException e) {
            logger.error("Validation error creating user", e);
            return new ResponseEntity<>(Map.of(
                "error", "Validation failed",
                "details", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return new ResponseEntity<>(Map.of(
                "error", "An error occurred while creating the user."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Map<String, Object>>> createUsersBulk(@RequestBody List<User> users) {
        logger.debug("POST /api/users/bulk called with users: {}", users);
        try {
            ResponseEntity<List<Map<String, Object>>> response = userService.createUsers(users);
            logger.info("Bulk user creation response: {}", response);
            return response;
        } catch (ConstraintViolationException e) {
            logger.error("Validation error creating users", e);
            return new ResponseEntity<>(List.of(
                Map.of(
                    "error", "Validation failed",
                    "details", e.getMessage()
                )
            ), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating users", e);
            return new ResponseEntity<>(List.of(
                Map.of(
                    "error", "An error occurred while creating users."
                )
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}