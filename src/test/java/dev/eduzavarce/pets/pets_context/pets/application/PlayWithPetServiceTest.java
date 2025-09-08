package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.*;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayWithPetServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    PlayWithPetService service;

    @Captor
    ArgumentCaptor<PetPostgresEntity> entityCaptor;

    @Test
    @DisplayName("Happy path: play decreases stamina by 10 and increases hunger by 10; saves updated entity")
    void happyPath_playUpdatesAndSaves() {
        String petId = "11111111-1111-1111-1111-111111111111";
        String ownerId = "22222222-2222-2222-2222-222222222222";

        // Initial: stamina 60, hunger 40 -> after play: stamina 50, hunger 50
        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 40, 60, PetType.CAT);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);

        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        Pet result = service.execute(petId, ownerId);

        assertThat(result.getStamina()).isEqualTo(50);
        assertThat(result.getHunger()).isEqualTo(50);

        verify(petRepository).save(entityCaptor.capture());
        PetPostgresEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(petId);
        assertThat(saved.getOwner()).isSameAs(owner);
    }

    @Test
    @DisplayName("Not found: throws NotFoundException and does not save")
    void notFound_throws() {
        String petId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String ownerId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pet not found");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Domain guard: stamina depleted -> LowStaminaException; no save")
    void staminaDepleted_throwsLowStamina() {
        String petId = "33333333-3333-3333-3333-333333333333";
        String ownerId = "44444444-4444-4444-4444-444444444444";

        // stamina 0 triggers LowStaminaException on play()
        PetDto dto = new PetDto(petId, "Tired", ownerId, 50, 40, 0, PetType.DOG);
        Pet domain = Pet.fromPrimitives(dto);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(LowStaminaException.class)
                .hasMessageContaining("stamina");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Domain guard: hunger maxed -> TooHungryException; no save")
    void hungerMaxed_throwsTooHungry() {
        String petId = "55555555-5555-5555-5555-555555555555";
        String ownerId = "66666666-6666-6666-6666-666666666666";

        // hunger 100 triggers TooHungryException on play()
        PetDto dto = new PetDto(petId, "Hungry", ownerId, 50, 100, 50, PetType.RABBIT);
        Pet domain = Pet.fromPrimitives(dto);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(TooHungryException.class)
                .hasMessageContaining("hungry");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Boundary: stamina floors at 0 and hunger caps at 100 on play")
    void boundary_staminaFloorAndHungerCap() {
        String petId = "77777777-7777-7777-7777-777777777777";
        String ownerId = "88888888-8888-8888-8888-888888888888";

        // stamina 10 -> 0; hunger 95 -> 100 after play
        PetDto dto = new PetDto(petId, "Edge", ownerId, 50, 95, 10, PetType.CANARY);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        Pet result = service.execute(petId, ownerId);

        assertThat(result.getStamina()).isEqualTo(0);
        assertThat(result.getHunger()).isEqualTo(100);
        verify(petRepository).save(any(PetPostgresEntity.class));
    }

    @Test
    @DisplayName("Repository.save throws -> propagate error")
    void repositorySaveThrows_propagate() {
        String petId = "99999999-9999-9999-9999-999999999999";
        String ownerId = "00000000-0000-0000-0000-000000000000";

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 30, 40, PetType.DOG);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));
        doThrow(new RuntimeException("db error")).when(petRepository).save(any(PetPostgresEntity.class));

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");
    }
}
