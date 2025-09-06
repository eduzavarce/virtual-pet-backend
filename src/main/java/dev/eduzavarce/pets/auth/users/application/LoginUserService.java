package dev.eduzavarce.pets.auth.users.application;

import dev.eduzavarce.pets.auth.users.infrastructure.JwtService;
import dev.eduzavarce.pets.shared.exceptions.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class LoginUserService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginUserService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String login(String usernameOrEmail, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
            );
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails userDetails)) {
                throw new AuthenticationException("Invalid username or password");
            }
            return jwtService.generateToken(userDetails);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
