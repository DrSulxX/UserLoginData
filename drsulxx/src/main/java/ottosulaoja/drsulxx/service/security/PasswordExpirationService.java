package ottosulaoja.drsulxx.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ottosulaoja.drsulxx.model.User;
import ottosulaoja.drsulxx.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordExpirationService {

    private static final int PASSWORD_EXPIRATION_DAYS = 90;

    @Autowired
    private UserRepository userRepository;

    public boolean isPasswordExpired(User user) {
        return ChronoUnit.DAYS.between(user.getPasswordLastChanged(), LocalDateTime.now()) > PASSWORD_EXPIRATION_DAYS;
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(newPassword);
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }
    
}