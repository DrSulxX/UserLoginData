package ottosulaoja.drsulxx.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Autowired
    private UserRepository userRepository;  // Repository for user-related operations

    @Autowired
    private UserSecurityRepository userSecurityRepository;  // Repository for security-related operations

    @Transactional
    public void loginSucceeded(String username) {
        UserSecurity userSecurity = findUserSecurityByUsername(username);
        resetFailedAttempts(userSecurity);
    }

    @Transactional
    public int loginFailed(String username) {
        UserSecurity userSecurity = findUserSecurityByUsername(username);
        int failedAttempts = userSecurity.getFailedLoginAttempts() + 1;
        userSecurity.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockUserAccount(userSecurity);
        }

        userSecurityRepository.save(userSecurity);
        return failedAttempts;
    }

    @Transactional
    public void lockUserAccount(UserSecurity userSecurity) {
        userSecurity.setAccountNonLocked(false);
        userSecurity.setLockoutTime(LocalDateTime.now());
        userSecurityRepository.save(userSecurity);
    }

    @Transactional
    public void unlockUserAccount(User user) {
        UserSecurity userSecurity = findUserSecurityByUserId(user.getId());
        userSecurity.setAccountNonLocked(true);
        userSecurity.setLockoutTime(null);
        userSecurity.setFailedLoginAttempts(0);
        userSecurityRepository.save(userSecurity);
    }

    public boolean isAccountLocked(String username) {
        UserSecurity userSecurity = findUserSecurityByUsername(username);
        if (!userSecurity.getAccountNonLocked()) {
            LocalDateTime lockoutTime = userSecurity.getLockoutTime();
            if (lockoutTime != null && LocalDateTime.now().isAfter(lockoutTime.plusMinutes(15))) {
                // Unlock the account using the user object fetched by user ID
                User user = userRepository.findById(userSecurity.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                unlockUserAccount(user);
                return false;
            }
            return true;
        }
        return false;
    }

    @Transactional
    public void resetFailedAttempts(UserSecurity userSecurity) {
        userSecurity.setFailedLoginAttempts(0);
        userSecurityRepository.save(userSecurity);
    }

    public int getMaxFailedAttempts() {
        return MAX_FAILED_ATTEMPTS;
    }

    private UserSecurity findUserSecurityByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userSecurityRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User security not found"));
    }

    private UserSecurity findUserSecurityByUserId(Long userId) {
        return userSecurityRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User security not found"));
    }
}