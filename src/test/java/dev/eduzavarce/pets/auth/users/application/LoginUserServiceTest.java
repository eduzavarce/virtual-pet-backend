package dev.eduzavarce.pets.auth.users.application;

import dev.eduzavarce.pets.auth.users.infrastructure.JwtService;
import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.shared.exceptions.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUserServiceTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;

    @InjectMocks
    LoginUserService service;

    @Captor
    ArgumentCaptor<Map<String, Object>> claimsCaptor;

    private Authentication authWithPrincipal(Object principal) {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return principal;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return principal instanceof UserDetails ud ? ud.getUsername() : "unknown";
            }
        };
    }

    @Test
    @DisplayName("Happy path: authenticates and generates token including userId claim when principal is UserPostgresEntity")
    void happyPath_includesUserIdClaim() {
        // Arrange
        String input = "john@example.com";
        String rawPassword = "Secret1!";
        UserPostgresEntity principal = mock(UserPostgresEntity.class);
        when(principal.getId()).thenReturn("uuid-123");
        // Only ID is required for claim enrichment; no need to stub UserDetails methods for this test.

        Authentication authentication = authWithPrincipal(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(jwtService.generateToken(any(Map.class), any(UserDetails.class))).thenReturn("jwt-token");

        // Act
        String token = service.login(input, rawPassword);

        // Assert
        assertThat(token).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(claimsCaptor.capture(), eq(principal));
        Map<String, Object> claims = claimsCaptor.getValue();
        assertThat(claims).containsEntry("userId", "uuid-123");
    }

    @Test
    @DisplayName("Principal is generic UserDetails -> no userId claim added")
    void genericUserDetails_noUserIdClaim() {
        String input = "john"; // username instead of email
        String rawPassword = "Secret1!";
        UserDetails principal = User.withUsername("john").password("hashed").roles("USER").build();

        Authentication authentication = authWithPrincipal(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(any(Map.class), any(UserDetails.class))).thenReturn("jwt-token-2");

        String token = service.login(input, rawPassword);

        assertThat(token).isEqualTo("jwt-token-2");
        verify(jwtService).generateToken(claimsCaptor.capture(), eq(principal));
        Map<String, Object> claims = claimsCaptor.getValue();
        assertThat(claims).doesNotContainKey("userId");
    }

    @Test
    @DisplayName("AuthenticationManager throws -> wraps into AuthenticationException with generic message")
    void authManagerThrows_wraps() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> service.login("someone", "pwd"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Principal is not UserDetails -> throws AuthenticationException")
    void principalNotUserDetails_throws() {
        Object principal = new Object();
        Authentication authentication = authWithPrincipal(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        assertThatThrownBy(() -> service.login("user", "pwd"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Accepts either email or username as input credential")
    void acceptsEmailOrUsername() {
        // email input
        Authentication authEmail = authWithPrincipal(User.withUsername("john@example.com").password("x").roles("USER").build());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authEmail);
        when(jwtService.generateToken(any(Map.class), any(UserDetails.class))).thenReturn("t1");
        String t1 = service.login("john@example.com", "pwd");
        assertThat(t1).isEqualTo("t1");

        // username input (second call)
        Authentication authUsername = authWithPrincipal(User.withUsername("john").password("x").roles("USER").build());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authUsername);
        when(jwtService.generateToken(any(Map.class), any(UserDetails.class))).thenReturn("t2");
        String t2 = service.login("john", "pwd");
        assertThat(t2).isEqualTo("t2");
    }

    @Test
    @DisplayName("JwtService.generateToken throws -> propagate error")
    void jwtServiceThrows_propagate() {
        UserDetails principal = User.withUsername("john").password("hashed").roles("USER").build();
        Authentication authentication = authWithPrincipal(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(any(Map.class), any(UserDetails.class))).thenThrow(new RuntimeException("signing failed"));

        assertThatThrownBy(() -> service.login("john", "pwd"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("signing failed");
    }
}
