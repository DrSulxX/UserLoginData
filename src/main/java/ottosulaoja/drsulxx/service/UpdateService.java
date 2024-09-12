package ottosulaoja.drsulxx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ottosulaoja.drsulxx.exception.DuplicateEntryException;
import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UpdateService {

    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);

    @Autowired
    public UpdateService(UserRepository userRepository, UserSecurityRepository userSecurityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User updateUserById(Long id, User user, UserSecurity userSecurityData) {
        try {
            Optional<User> existingUserOpt = userRepository.findById(id);
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                updateUserFields(existingUser, user);

                // Update UserSecurity fields using userId
                Optional<UserSecurity> userSecurityOpt = userSecurityRepository.findByUserId(existingUser.getId());
                if (userSecurityOpt.isPresent()) {
                    UserSecurity userSecurity = userSecurityOpt.get();
                    updateUserSecurityFields(userSecurity, userSecurityData);
                }

                return userRepository.save(existingUser);
            } else {
                logger.warn("User with ID {} not found.", id);
                return null;
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("Duplicate entry detected when updating user by ID: {}", id);  // No stack trace
            throw new DuplicateEntryException("Duplicate entry for email or username.");
        } catch (Exception e) {
            logger.error("Error updating user by ID: " + id, e);  // Full stack trace for other errors
            throw e;
        }
    }

    public List<User> updateUserByUsername(String username, User user, UserSecurity userSecurityData) {
        try {
            List<User> existingUsers = userRepository.findByUsernameContainingIgnoreCase(username);
            if (!existingUsers.isEmpty()) {
                for (User existingUser : existingUsers) {
                    updateUserFields(existingUser, user);

                    // Update UserSecurity fields using userId
                    Optional<UserSecurity> userSecurityOpt = userSecurityRepository.findByUserId(existingUser.getId());
                    if (userSecurityOpt.isPresent()) {
                        UserSecurity userSecurity = userSecurityOpt.get();
                        updateUserSecurityFields(userSecurity, userSecurityData);
                    }

                    userRepository.save(existingUser);
                }
                return existingUsers;
            } else {
                logger.warn("User with username {} not found.", username);
                return List.of();
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("Duplicate entry detected when updating user by username: {}", username);
            throw new DuplicateEntryException("Duplicate entry for email or username.");
        } catch (Exception e) {
            logger.error("Error updating user by username: " + username, e);
            throw e;
        }
    }

    public List<User> updateUserByEmail(String email, User user, UserSecurity userSecurityData) {
        try {
            List<User> existingUsers = userRepository.findByEmailIgnoreCase(email);
            if (!existingUsers.isEmpty()) {
                for (User existingUser : existingUsers) {
                    updateUserFields(existingUser, user);

                    // Update UserSecurity fields using userId
                    Optional<UserSecurity> userSecurityOpt = userSecurityRepository.findByUserId(existingUser.getId());
                    if (userSecurityOpt.isPresent()) {
                        UserSecurity userSecurity = userSecurityOpt.get();
                        updateUserSecurityFields(userSecurity, userSecurityData);
                    }

                    userRepository.save(existingUser);
                }
                return existingUsers;
            } else {
                logger.warn("User with email {} not found.", email);
                return List.of();
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("Duplicate entry detected when updating user by email: {}", email);
            throw new DuplicateEntryException("Duplicate entry for email.");
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
            // Update passwordLastChanged in UserSecurity
            Optional<UserSecurity> userSecurityOpt = userSecurityRepository.findByUserId(existingUser.getId());
            if (userSecurityOpt.isPresent()) {
                UserSecurity userSecurity = userSecurityOpt.get();
                userSecurity.setPasswordLastChanged(LocalDateTime.now());
                userSecurityRepository.save(userSecurity);
            }
        }
        // Update updatedAt field
        existingUser.setUpdatedAt(LocalDateTime.now());
    }

    private void updateUserSecurityFields(UserSecurity userSecurity, UserSecurity newUserSecurityData) {
        // Access security-related data using UserSecurity directly
        if (newUserSecurityData.getEnabled() != null) {
            userSecurity.setEnabled(newUserSecurityData.getEnabled());
        }
        if (newUserSecurityData.getAccountNonExpired() != null) {
            userSecurity.setAccountNonExpired(newUserSecurityData.getAccountNonExpired());
        }
        if (newUserSecurityData.getCredentialsNonExpired() != null) {
            userSecurity.setCredentialsNonExpired(newUserSecurityData.getCredentialsNonExpired());
        }
        if (newUserSecurityData.getAccountNonLocked() != null) {
            userSecurity.setAccountNonLocked(newUserSecurityData.getAccountNonLocked());
        }
        // Save the updated user security information
        userSecurityRepository.save(userSecurity);
    }
}