package ottosulaoja.drsulxx.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling various API endpoints.
 * <p>
 * This class demonstrates endpoints that can be used for different access levels:
 * - Public endpoint accessible by anyone.
 * - Admin endpoint requires authentication.
 */
@RestController
@RequestMapping("/api")
public class AdminController {

    /**
     * Endpoint for accessing admin-specific resources.
     * <p>
     * This endpoint is intended for authenticated users with admin privileges.
     * It returns a simple string message indicating successful access to the admin endpoint.
     * 
     * @return A string message indicating that the admin endpoint was accessed.
     */
    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin endpoint accessed!";
    }

    /**
     * Public endpoint accessible without authentication.
     * <p>
     * This endpoint is available to all users and provides a simple message indicating
     * that the public endpoint has been accessed. Useful for public-facing resources.
     * 
     * @return A string message indicating that the public endpoint was accessed.
     */
    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public endpoint accessed!";
    }
}