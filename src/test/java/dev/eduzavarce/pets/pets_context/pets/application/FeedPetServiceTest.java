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
class FeedPetServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    FeedPetService service;

    @Captor
    ArgumentCaptor<PetPostgresEntity> entityCaptor;

    @Test
    @DisplayName("Happy path: finds pet by id & owner, feeds (hunger -10), saves updated entity, returns updated domain")
    void happyPath_feedsAndSaves() {
        String petId = "11111111-1111-1111-1111-111111111111";
        String ownerId = "22222222-2222-2222-2222-222222222222";

        // Arrange: existing entity with hunger 60
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 60, 50, PetType.CAT);
        Pet domain = Pet.fromPrimitives(dto);

        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        // Act
        Pet result = service.execute(petId, ownerId);

        // Assert domain changes
        assertThat(result.getHunger()).isEqualTo(50); // 60 -> feed() -> 50

        // Saved entity created from updated pet and same owner
        verify(petRepository).save(entityCaptor.capture());
        PetPostgresEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(petId);
        assertThat(saved.getOwner()).isSameAs(owner);
    }

    @Test
    @DisplayName("Corner: not found -> throws NotFoundException and does not save")
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
    @DisplayName("Corner: repository.save throws -> propagate and do not swallow errors")
    void repositorySaveThrows_propagate() {
        String petId = "33333333-3333-3333-3333-333333333333";
        String ownerId = "44444444-4444-4444-4444-444444444444";

        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 20, 50, PetType.DOG);
        Pet domain = Pet.fromPrimitives(dto);

        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));
        doThrow(new RuntimeException("db error")).when(petRepository).save(any(PetPostgresEntity.class));

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");
    }

    @Test
    @DisplayName("Boundary: hunger does not go below 0 when feeding")
    void boundary_hungerClampedAtZero() {
        String petId = "55555555-5555-5555-5555-555555555555";
        String ownerId = "66666666-6666-6666-6666-666666666666";

        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);

        // hunger 5 -> feed() should clamp to 0
        PetDto dto = new PetDto(petId, "Tiny", ownerId, 50, 5, 50, PetType.CANARY);
        Pet domain = Pet.fromPrimitives(dto);

        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        Pet result = service.execute(petId, ownerId);

        assertThat(result.getHunger()).isEqualTo(0);
        verify(petRepository).save(any(PetPostgresEntity.class));
    }
}
