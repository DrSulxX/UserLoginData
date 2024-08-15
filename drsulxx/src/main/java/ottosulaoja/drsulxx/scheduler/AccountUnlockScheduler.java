package ottosulaoja.drsulxx.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.repository.UserRepository;
import ottosulaoja.drsulxx.service.security.LoginAttemptService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class AccountUnlockScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void unlockAccounts() {
        LocalDateTime now = LocalDateTime.now();
        // Query to find only locked users
        List<User> lockedUsers = userRepository.findByAccountNonLockedFalse();

        for (User user : lockedUsers) {
            if (user.getLockoutTime() != null && ChronoUnit.MINUTES.between(user.getLockoutTime(), now) >= 5) {
                loginAttemptService.unlockUserAccount(user);
            }
        }
    }
}