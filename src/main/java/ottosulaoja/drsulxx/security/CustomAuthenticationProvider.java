package ottosulaoja.drsulxx.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ottosulaoja.drsulxx.model.security.UserSecurity;
// Import statements for user management
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.security.UserSecurityRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;



@Service // Ensure this annotation is present
public class CustomAuthenticationProvider implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        UserSecurity userSecurity = userSecurityRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Security details not found for username: " + username));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            userSecurity.getEnabled(),
            userSecurity.getAccountNonExpired(),
            userSecurity.getCredentialsNonExpired(),
            userSecurity.getAccountNonLocked(),
            user.getAuthorities()
        );
    }
}