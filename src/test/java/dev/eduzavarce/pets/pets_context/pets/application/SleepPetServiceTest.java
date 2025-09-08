package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.domain.PetDto;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
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
class SleepPetServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    SleepPetService service;

    @Captor
    ArgumentCaptor<PetPostgresEntity> entityCaptor;

    @Test
    @DisplayName("Happy path: sleep increases stamina by 30 and saves updated entity")
    void happyPath_sleepIncreasesStaminaAndSaves() {
        String petId = "11111111-1111-1111-1111-111111111111";
        String ownerId = "22222222-2222-2222-2222-222222222222";

        // Initial: stamina 40 -> after sleep: 70
        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 50, 40, PetType.CAT);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);

        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        Pet result = service.execute(petId, ownerId);

        assertThat(result.getStamina()).isEqualTo(70);
        verify(petRepository).save(entityCaptor.capture());
        PetPostgresEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(petId);
        assertThat(saved.getOwner()).isSameAs(owner);
    }

    @Test
    @DisplayName("Boundary: stamina caps at 100 when sleeping")
    void boundary_staminaCapsAt100() {
        String petId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String ownerId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

        // stamina 80 -> sleep: +30 -> 100 (cap)
        PetDto dto = new PetDto(petId, "Edge", ownerId, 50, 50, 80, PetType.DOG);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);

        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        Pet result = service.execute(petId, ownerId);

        assertThat(result.getStamina()).isEqualTo(100);
        verify(petRepository).save(any(PetPostgresEntity.class));
    }

    @Test
    @DisplayName("Not found: throws NotFoundException and does not save")
    void notFound_throws() {
        String petId = "33333333-3333-3333-3333-333333333333";
        String ownerId = "44444444-4444-4444-4444-444444444444";

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pet not found");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Repository.save throws -> propagate error")
    void repositorySaveThrows_propagate() {
        String petId = "55555555-5555-5555-5555-555555555555";
        String ownerId = "66666666-6666-6666-6666-666666666666";

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 50, 20, PetType.RABBIT);
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
