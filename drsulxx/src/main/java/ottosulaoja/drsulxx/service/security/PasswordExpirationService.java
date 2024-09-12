package ottosulaoja.drsulxx.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class PasswordExpirationService {

    private static final int PASSWORD_EXPIRATION_DAYS = 90;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Check if the password is expired based on the last changed date.
     * @param userSecurity The UserSecurity object containing password details.
     * @return true if the password is expired, false otherwise.
     */
    public boolean isPasswordExpired(UserSecurity userSecurity) {
        // Ensure passwordLastChanged is not null before comparing
        if (userSecurity.getPasswordLastChanged() == null) {
            return true;
        }
        return ChronoUnit.DAYS.between(userSecurity.getPasswordLastChanged(), LocalDateTime.now()) > PASSWORD_EXPIRATION_DAYS;
    }

    /**
     * Update the password in the UserSecurity object.
     * @param userSecurity The UserSecurity object to update.
     * @param newPassword The new password to set.
     */
    public void updatePassword(UserSecurity userSecurity, String newPassword) {
        userSecurity.setPasswordLastChanged(LocalDateTime.now());
        userSecurity.setCredentialsNonExpired(true);
        userSecurityRepository.save(userSecurity);
    }
    
    /**
     * Update the password for the User and UserSecurity objects.
     * @param user The User object to update.
     * @param newPassword The new password to set.
     */
    public void updatePassword(User user, String newPassword) {
        UserSecurity userSecurity = userSecurityRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User security not found"));

        // Ensure username and email are unique before updating
        validateUserUniqueness(user);

        // Update password in User object
        user.setPassword(passwordEncoder.encode(newPassword));
        userSecurity.setPasswordLastChanged(LocalDateTime.now());
        userSecurity.setCredentialsNonExpired(true);

        // Save updated User and UserSecurity
        userRepository.save(user); // Save user with updated password
        userSecurityRepository.save(userSecurity); // Save userSecurity
    }

    /**
     * Validate the uniqueness of the username and email of the User.
     * @param user The User object to validate.
     * @throws IllegalArgumentException if username or email already exists for another user.
     */
    private void validateUserUniqueness(User user) {
        Optional<User> existingUserByUsername = userRepository.findByUsername(user.getUsername());
        Optional<User> existingUserByEmail = userRepository.findByEmail(user.getEmail());

        if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        
        if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Email already exists.");
        }
    }
}