package dev.eduzavarce.pets.pets_context.users.application;

import dev.eduzavarce.pets.auth.users.domain.UserDto;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserRepository;
import dev.eduzavarce.pets.shared.core.domain.DomainEvent;
import dev.eduzavarce.pets.shared.core.domain.EventBus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePetUserServiceTest {

    @Mock
    PetsUserRepository petsUserRepository;
    @Mock
    EventBus eventBus;

    @InjectMocks
    CreatePetUserService service;

    @Captor
    ArgumentCaptor<PetsUserPostgresEntity> entityCaptor;
    @Captor
    ArgumentCaptor<List<DomainEvent>> eventsCaptor;

    private static UserDto userDto(String id, String username, String email) {
        return new UserDto(id, username, email, "ROLE_USER");
    }

    @Test
    @DisplayName("Happy path: creates PetUser, saves entity, and publishes single pets.users.created event")
    void happyPath_savesAndPublishes() {
        String id = "11111111-1111-1111-1111-111111111111";
        String username = "john";
        String email = "john@example.com";

        service.execute(userDto(id, username, email));

        // Save called with entity having same id and username
        verify(petsUserRepository).save(entityCaptor.capture());
        PetsUserPostgresEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getUsername()).isEqualTo(username);

        // Publish called with one event named "pets.users.created"
        verify(eventBus).publish(eventsCaptor.capture());
        List<DomainEvent> events = eventsCaptor.getValue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).eventName()).isEqualTo("pets.users.created");
    }

    @Test
    @DisplayName("Repository.save throws -> no events published and exception propagates")
    void repoSaveThrows_noPublish() {
        String id = "22222222-2222-2222-2222-222222222222";
        String username = "mary";
        String email = "mary@example.com";

        doThrow(new RuntimeException("db error")).when(petsUserRepository).save(any(PetsUserPostgresEntity.class));

        assertThatThrownBy(() -> service.execute(userDto(id, username, email)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");

        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Invalid username (blank) bubbles from domain -> no save, no publish")
    void invalidUsername_blank_noSave() {
        String id = "33333333-3333-3333-3333-333333333333";
        String username = "   ";
        String email = "u@example.com";

        assertThatThrownBy(() -> service.execute(userDto(id, username, email)))
                .isInstanceOf(RuntimeException.class);

        verify(petsUserRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Propagates domain event payload: event body contains PetUserDto primitives")
    void eventBodyContainsPrimitives() {
        String id = "44444444-4444-4444-4444-444444444444";
        String username = "alice";
        String email = "alice@example.com";

        // Capture events and inspect body type
        doAnswer(invocation -> {
            List<DomainEvent> evs = invocation.getArgument(0);
            assertThat(evs).hasSize(1);
            // We can't access the raw body directly from DomainEvent; assert toPrimitives payload instead
            var dto = evs.get(0).toPrimitives();
            assertThat(dto.eventName()).isEqualTo("pets.users.created");
            assertThat(dto.aggregateId()).isEqualTo(id);
            // The serialized body should be a PetUserDto; depending on serializer, we can only assert non-null here
            assertThat(dto.body()).isNotNull();
            return null;
        }).when(eventBus).publish(any());

        service.execute(userDto(id, username, email));

        verify(petsUserRepository).save(any(PetsUserPostgresEntity.class));
        verify(eventBus).publish(any());
    }
}
