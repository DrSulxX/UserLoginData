package ottosulaoja.drsulxx.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Autowired
    private UserRepository userRepository;

    public void loginSucceeded(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        resetFailedAttempts(user);
    }

    public int loginFailed(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockUserAccount(user);
        }
        
        userRepository.save(user);

        return failedAttempts;
    }

    public void lockUserAccount(User user) {
        user.setAccountNonLocked(false);
        user.setLockoutTime(LocalDateTime.now());
        userRepository.save(user);
    }

    public void unlockUserAccount(User user) {
        user.setAccountNonLocked(true);
        user.setLockoutTime(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    public boolean isAccountLocked(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getAccountNonLocked()) {
            LocalDateTime lockoutTime = user.getLockoutTime();
            if (lockoutTime != null && LocalDateTime.now().isAfter(lockoutTime.plusMinutes(15))) {
                unlockUserAccount(user);
                return false;
            }
            return true;
        }
        return false;
    }

    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }
}