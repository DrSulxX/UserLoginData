package ottosulaoja.drsulxx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UpdateService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);

    @Autowired
    public UpdateService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User updateUserById(Long id, User user) {
        try {
            Optional<User> existingUserOpt = userRepository.findById(id);
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                updateUserFields(existingUser, user);
                return userRepository.save(existingUser);
            } else {
                logger.warn("User with ID {} not found.", id);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error updating user by ID: " + id, e);
            throw e;
        }
    }

    public List<User> updateUserByUsername(String username, User user) {
        try {
            List<User> existingUsers = userRepository.findByUsernameContainingIgnoreCase(username);
            if (!existingUsers.isEmpty()) {
                for (User existingUser : existingUsers) {
                    updateUserFields(existingUser, user);
                    userRepository.save(existingUser);
                }
                return existingUsers;
            } else {
                logger.warn("User with username {} not found.", username);
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Error updating user by username: " + username, e);
            throw e;
        }
    }

    public List<User> updateUserByEmail(String email, User user) {
        try {
            List<User> existingUsers = userRepository.findByEmailIgnoreCase(email);
            if (!existingUsers.isEmpty()) {
                for (User existingUser : existingUsers) {
                    updateUserFields(existingUser, user);
                    userRepository.save(existingUser);
                }
                return existingUsers;
            } else {
                logger.warn("User with email {} not found.", email);
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Error updating user by email: " + email, e);
            throw e;
        }
    }

    private void updateUserFields(User existingUser, User newUserData) {
        if (newUserData.getUsername() != null && !newUserData.getUsername().isEmpty()) {
            existingUser.setUsername(newUserData.getUsername());
        }
        if (newUserData.getEmail() != null && !newUserData.getEmail().isEmpty()) {
            existingUser.setEmail(newUserData.getEmail());
        }
        if (newUserData.getName() != null && !newUserData.getName().isEmpty()) {
            existingUser.setName(newUserData.getName());
        }
        if (newUserData.getFamilyName() != null && !newUserData.getFamilyName().isEmpty()) {
            existingUser.setFamilyName(newUserData.getFamilyName());
        }
        // Ensure BCrypt password encoding
        if (newUserData.getPassword() != null && !newUserData.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newUserData.getPassword()));
        }
        if (newUserData.getEnabled() != null) {
            existingUser.setEnabled(newUserData.getEnabled());
        }
        if (newUserData.getAccountNonExpired() != null) {
            existingUser.setAccountNonExpired(newUserData.getAccountNonExpired());
        }
        if (newUserData.getCredentialsNonExpired() != null) {
            existingUser.setCredentialsNonExpired(newUserData.getCredentialsNonExpired());
        }
        if (newUserData.getAccountNonLocked() != null) {
            existingUser.setAccountNonLocked(newUserData.getAccountNonLocked());
        }
    }
}