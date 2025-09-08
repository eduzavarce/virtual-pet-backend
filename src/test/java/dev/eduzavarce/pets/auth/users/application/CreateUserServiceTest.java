package dev.eduzavarce.pets.auth.users.application;

import dev.eduzavarce.pets.auth.users.domain.CreateUserDto;
import dev.eduzavarce.pets.auth.users.domain.PasswordHasher;
import dev.eduzavarce.pets.auth.users.infrastructure.AuthUserRepository;
import dev.eduzavarce.pets.auth.users.infrastructure.CreateUserRequest;
import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.shared.core.domain.DomainEvent;
import dev.eduzavarce.pets.shared.core.domain.EventBus;
import dev.eduzavarce.pets.shared.exceptions.AlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    PasswordHasher passwordHasher;
    @Mock
    AuthUserRepository authUserRepository;
    @Mock
    EventBus eventBus;

    @InjectMocks
    CreateUserService service;

    @Captor
    ArgumentCaptor<UserPostgresEntity> entityCaptor;
    @Captor
    ArgumentCaptor<List<DomainEvent>> eventsCaptor;

    String id;
    String username;
    String email;
    String password;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID().toString();
        username = "john";
        email = "john@example.com";
        password = "secret";
    }

    private CreateUserRequest request() {
        return new CreateUserRequest(id, username, email, password, password);
    }

    @Test
    @DisplayName("Happy path: hashes password, checks existence, saves, and publishes event")
    void happyPath() {
        when(passwordHasher.hash(password)).thenReturn("hashed");
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(email)).thenReturn(Optional.empty());

        service.createUser(request());

        verify(authUserRepository).save(entityCaptor.capture());
        UserPostgresEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        // In this entity, UserDetails#getUsername is the email field
        assertThat(saved.getUsername()).isEqualTo(email);
        assertThat(saved.getPassword()).isEqualTo("hashed");

        verify(eventBus).publish(eventsCaptor.capture());
        List<DomainEvent> events = eventsCaptor.getValue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).eventName()).isEqualTo("user.created");

        // Ensure persistence is mocked: no DB interaction beyond repository mock
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    @DisplayName("Corner: existing id causes AlreadyExistsException and does not save or publish")
    void existingId() {
        when(passwordHasher.hash(password)).thenReturn("hashed");
        when(authUserRepository.findById(id)).thenReturn(Optional.of(new UserPostgresEntity(new CreateUserDto(id, username, email, "h"))));

        assertThatThrownBy(() -> service.createUser(request()))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(authUserRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Corner: existing email causes AlreadyExistsException and does not save or publish")
    void existingEmail() {
        when(passwordHasher.hash(password)).thenReturn("hashed");
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(email)).thenReturn(Optional.of(new UserPostgresEntity(new CreateUserDto(id, username, email, "h"))));

        assertThatThrownBy(() -> service.createUser(request()))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining(email);

        verify(authUserRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Corner: password is hashed exactly once and passed through")
    void passwordHashedOnce() {
        when(passwordHasher.hash(password)).thenReturn("HASH");
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(email)).thenReturn(Optional.empty());

        service.createUser(request());

        verify(passwordHasher, times(1)).hash(password);
        verify(authUserRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getPassword()).isEqualTo("HASH");
    }

    @Test
    @DisplayName("Corner: invalid input from domain (e.g., bad email) bubbles up and does not persist")
    void invalidDomainInputBubblesUp() {
        String badEmail = "not-an-email";
        when(passwordHasher.hash(password)).thenReturn("HASH");
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(badEmail)).thenReturn(Optional.empty());

        CreateUserRequest badReq = new CreateUserRequest(id, username, badEmail, password, password);

        assertThatThrownBy(() -> service.createUser(badReq))
                .isInstanceOf(RuntimeException.class);

        verify(authUserRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Corner: repository.save throws -> event is not published (fail-fast)")
    void repoSaveThrows_noEventPublished() {
        when(passwordHasher.hash(password)).thenReturn("HASH");
        when(authUserRepository.findById(id)).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(email)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("db down")).when(authUserRepository).save(any());

        assertThatThrownBy(() -> service.createUser(request()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db down");

        verify(eventBus, never()).publish(any());
    }
}
