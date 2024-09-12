package ottosulaoja.drsulxx.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ottosulaoja.drsulxx.model.security.UserSecurity;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;
import ottosulaoja.drsulxx.service.security.LoginAttemptService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class AccountUnlockScheduler {

    @Autowired
    private UserSecurityRepository userSecurityRepository; // Correct repository for UserSecurity

    @Autowired
    private UserRepository userRepository; // Correct repository for User

    @Autowired
    private LoginAttemptService loginAttemptService; // Service to handle login attempts

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void unlockAccounts() {
        LocalDateTime now = LocalDateTime.now();
        
        // Query to find only locked users in UserSecurity table
        List<UserSecurity> lockedUsers = userSecurityRepository.findByAccountNonLockedFalse();

        for (UserSecurity userSecurity : lockedUsers) {
            if (userSecurity.getLockoutTime() != null && ChronoUnit.MINUTES.between(userSecurity.getLockoutTime(), now) >= 5) {
                // Fetch the User entity associated with the UserSecurity
                Long userId = userSecurity.getUserId(); 
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

                loginAttemptService.unlockUserAccount(user);  // Pass the fetched User entity
            }
        }
    }
}