package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.domain.PetDto;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.pets_context.pets.domain.PetWithOwnerDto;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPetByIdServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    GetPetByIdService service;

    @Test
    @DisplayName("Happy path: returns PetWithOwnerDto composed from domain pet and owner info")
    void happyPath_returnsDto() {
        String petId = "11111111-1111-1111-1111-111111111111";
        String ownerId = "22222222-2222-2222-2222-222222222222";

        // Arrange entity + owner
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        when(owner.getId()).thenReturn(ownerId);
        when(owner.getUsername()).thenReturn("johnny");

        PetDto dto = new PetDto(petId, "Fluffy", ownerId, 70, 40, 90, PetType.DOG);
        Pet domain = Pet.fromPrimitives(dto);

        PetPostgresEntity entity = mock(PetPostgresEntity.class);
        when(entity.getId()).thenReturn(petId);
        when(entity.getOwner()).thenReturn(owner);
        when(entity.toDomain()).thenReturn(domain);

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(entity));

        // Act
        PetWithOwnerDto result = service.execute(petId, ownerId);

        // Assert result fields
        assertThat(result.id()).isEqualTo(petId);
        assertThat(result.name()).isEqualTo("Fluffy");
        assertThat(result.ownerId()).isEqualTo(ownerId);
        assertThat(result.ownerUsername()).isEqualTo("johnny");
        assertThat(result.health()).isEqualTo(70);
        assertThat(result.hunger()).isEqualTo(40);
        assertThat(result.stamina()).isEqualTo(90);
        assertThat(result.type()).isEqualTo(PetType.DOG);

        verify(petRepository).findByIdAndOwner_Id(petId, ownerId);
        verifyNoMoreInteractions(petRepository);
    }

    @Test
    @DisplayName("Not found: throws NotFoundException and does not proceed")
    void notFound_throws() {
        String petId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String ownerId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pet not found");

        verify(petRepository).findByIdAndOwner_Id(petId, ownerId);
        verifyNoMoreInteractions(petRepository);
    }
}