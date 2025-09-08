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
class RenamePetServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    RenamePetService service;

    @Captor
    ArgumentCaptor<PetPostgresEntity> entityCaptor;

    @Test
    @DisplayName("Happy path: renames pet and saves updated entity with same owner")
    void happyPath_renamesAndSaves() {
        String petId = "11111111-1111-1111-1111-111111111111";
        String ownerId = "22222222-2222-2222-2222-222222222222";
        String newName = "Sir Fluffy";

        // Existing pet
        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 70, 40, 90, PetType.CAT);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);

        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        // Act
        service.execute(petId, ownerId, newName);

        // Assert saved entity was created from updated domain and same owner
        verify(petRepository).save(entityCaptor.capture());
        PetPostgresEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(petId);
        assertThat(saved.getOwner()).isSameAs(owner);

        // Domain state updated
        Pet updated = existing.toDomain(); // our domain instance was mutated by service
        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Not found: throws NotFoundException and does not save")
    void notFound_throws() {
        String petId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String ownerId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(petId, ownerId, "Any"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pet not found");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invalid name: blank -> IllegalArgumentException; no save")
    void invalidName_blank() {
        String petId = "33333333-3333-3333-3333-333333333333";
        String ownerId = "44444444-4444-4444-4444-444444444444";

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 50, 50, PetType.DOG);
        Pet domain = Pet.fromPrimitives(dto);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(petId, ownerId, "  \t  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invalid name: too long (>30) -> IllegalArgumentException; no save")
    void invalidName_tooLong() {
        String petId = "55555555-5555-5555-5555-555555555555";
        String ownerId = "66666666-6666-6666-6666-666666666666";

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 50, 50, PetType.RABBIT);
        Pet domain = Pet.fromPrimitives(dto);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        String tooLong = "A".repeat(31);
        assertThatThrownBy(() -> service.execute(petId, ownerId, tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too long");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Boundary: name length 30 succeeds and persists")
    void boundary_maxLength30() {
        String petId = "77777777-7777-7777-7777-777777777777";
        String ownerId = "88888888-8888-8888-8888-888888888888";

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 50, 50, PetType.CANARY);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        String max30 = "123456789012345678901234567890"; // 30 chars
        service.execute(petId, ownerId, max30);

        verify(petRepository).save(any(PetPostgresEntity.class));
        assertThat(domain.getName()).isEqualTo(max30);
    }

    @Test
    @DisplayName("Unicode name succeeds and persists")
    void unicodeName_succeeds() {
        String petId = "99999999-9999-9999-9999-999999999999";
        String ownerId = "00000000-0000-0000-0000-000000000000";

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 50, 50, PetType.DOG);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        String unicode = "Níño 🐶";
        service.execute(petId, ownerId, unicode);

        verify(petRepository).save(any(PetPostgresEntity.class));
        assertThat(domain.getName()).isEqualTo(unicode);
    }

    @Test
    @DisplayName("Repository.save throws -> propagate error; rename attempted")
    void repositorySaveThrows_propagate() {
        String petId = "12121212-1212-1212-1212-121212121212";
        String ownerId = "34343434-3434-3434-3434-343434343434";

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 50, 50, 50, PetType.CAT);
        Pet domain = Pet.fromPrimitives(dto);
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        PetPostgresEntity existing = mock(PetPostgresEntity.class);
        when(existing.toDomain()).thenReturn(domain);
        when(existing.getOwner()).thenReturn(owner);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(existing));

        doThrow(new RuntimeException("db error")).when(petRepository).save(any(PetPostgresEntity.class));

        assertThatThrownBy(() -> service.execute(petId, ownerId, "New Name"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");

        // Domain was updated before save attempt
        assertThat(domain.getName()).isEqualTo("New Name");
    }
}
