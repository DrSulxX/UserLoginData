package ottosulaoja.drsulxx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticateUser(String username, String password) {
        logger.info("Authenticating user: {}", username);

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            logger.warn("User not found: {}", username);
            return false;
        }

        // Use PasswordEncoder to check if the raw password matches the hashed password in the database
        boolean isPasswordMatch = passwordEncoder.matches(password, user.getPassword());
        if (!isPasswordMatch) {
            logger.warn("Invalid password for user: {}", username);
        } else {
            logger.info("User authenticated: {}", username);
        }

        return isPasswordMatch;
    }
}