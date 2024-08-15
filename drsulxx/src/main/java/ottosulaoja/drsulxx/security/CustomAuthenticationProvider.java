package ottosulaoja.drsulxx.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.logging.Logger;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @Value("${spring.security.user.roles}")
    private String roles;

    private static final Logger logger = Logger.getLogger(CustomAuthenticationProvider.class.getName());

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String inputUsername = authentication.getName();
        String inputPassword = authentication.getCredentials().toString();

        logger.info("Authenticating user: " + inputUsername);

        logger.info("Expected username: " + username);
        logger.info("Expected password: " + password);

        if (username.equals(inputUsername) && password.equals(inputPassword)) {
            logger.info("Authentication successful for user: " + inputUsername);
            return new UsernamePasswordAuthenticationToken(inputUsername, inputPassword,
                    Collections.singletonList(new SimpleGrantedAuthority(roles)));
        } else {
            logger.info("Authentication failed for user: " + inputUsername);
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}