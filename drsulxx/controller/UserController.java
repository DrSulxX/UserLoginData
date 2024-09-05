package ottosulaoja.drsulxx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.service.UserService;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody User user) {
        logger.debug("POST /api/users called with user: {}", user);
        try {
            ResponseEntity<Map<String, Object>> response = userService.createUser(user);
            logger.info("User creation response: {}", response);
            return response;
        } catch (ConstraintViolationException e) {
            logger.error("Validation error creating user", e);
            return createErrorResponse("Validation failed", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return createErrorResponse("An error occurred while creating the user.", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Map<String, Object>>> createUsersBulk(@Valid @RequestBody List<User> users) {
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
                    "error", "An error occurred while creating users.",
                    "details", e.getMessage()
                )
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates an error response with a standardized structure.
     *
     * @param error   The error message.
     * @param details The detailed error message.
     * @param status  The HTTP status to return.
     * @return A ResponseEntity containing the error information.
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String error, String details, HttpStatus status) {
        return new ResponseEntity<>(Map.of(
            "error", error,
            "details", details
        ), status);
    }
}