package dev.eduzavarce.pets.auth.users.application;

import dev.eduzavarce.pets.auth.users.infrastructure.JwtService;
import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.shared.exceptions.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
            Map<String, Object> claims = new HashMap<>();
            // Include userId claim when principal is our entity
            if (principal instanceof UserPostgresEntity userEntity) {
                claims.put("userId", userEntity.getId());
            }
            return jwtService.generateToken(claims, userDetails);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
