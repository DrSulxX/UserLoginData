package ottosulaoja.drsulxx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ottosulaoja.drsulxx.model.passwordreset.UserPasswordReset;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.repository.passwordreset.UserPasswordResetRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPasswordResetRepository userPasswordResetRepository;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Autowired
    private PasswordResetEmailService passwordResetEmailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(transactionManager = "securityTransactionManager")
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();  // Generate a unique token
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(20);  // Set expiration to 20 minutes

        Optional<UserPasswordReset> resetOptional = userPasswordResetRepository.findByUserId(user.getId());
        UserPasswordReset userPasswordReset;
        if (resetOptional.isPresent()) {
            userPasswordReset = resetOptional.get();
        } else {
            userPasswordReset = new UserPasswordReset();
            userPasswordReset.setUserId(user.getId());
        }

        userPasswordReset.setResetToken(token);
        userPasswordReset.setResetTokenExpiration(expirationTime);
        userPasswordResetRepository.save(userPasswordReset);

        passwordResetEmailService.sendPasswordResetCodeEmail(user.getEmail(), token);
    }

    @Transactional(transactionManager = "securityTransactionManager", readOnly = true)
    public boolean verifyResetToken(String token) {
        Optional<UserPasswordReset> resetOptional = userPasswordResetRepository.findByResetToken(token);

        if (resetOptional.isEmpty() || resetOptional.get().getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            logger.warn("Invalid or expired reset token for token: {}", token);
            return false;
        }

        return true;
    }

    @Transactional(transactionManager = "securityTransactionManager")
    public void resetPassword(String token, String newPassword) {
        Optional<UserPasswordReset> resetOptional = userPasswordResetRepository.findByResetToken(token);

        if (resetOptional.isEmpty() || resetOptional.get().getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            logger.warn("Invalid or expired reset token for token: {}", token);
            throw new RuntimeException("Invalid or expired reset token.");
        }

        UserPasswordReset userPasswordReset = resetOptional.get();
        User user = userRepository.findById(userPasswordReset.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // Update timestamp
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Update UserSecurity fields
        Optional<UserSecurity> userSecurityOpt = userSecurityRepository.findByUserId(user.getId());
        if (userSecurityOpt.isPresent()) {
            UserSecurity userSecurity = userSecurityOpt.get();
            userSecurity.setPasswordLastChanged(LocalDateTime.now());
            userSecurityRepository.save(userSecurity);
        }

        userPasswordReset.setResetToken(null);
        userPasswordReset.setResetTokenExpiration(null);
        userPasswordResetRepository.save(userPasswordReset);
    }
}