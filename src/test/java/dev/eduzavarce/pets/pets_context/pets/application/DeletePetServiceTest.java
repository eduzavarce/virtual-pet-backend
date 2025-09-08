package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePetServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    DeletePetService service;

    @Captor
    ArgumentCaptor<PetPostgresEntity> entityCaptor;

    @Test
    @DisplayName("Happy path: deletes pet enforcing ownership (id + ownerId)")
    void deletesOwnedPet() {
        String petId = "pet-1";
        String ownerId = "owner-1";
        PetPostgresEntity entity = mock(PetPostgresEntity.class);
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.of(entity));

        service.execute(petId, ownerId);

        verify(petRepository).delete(entityCaptor.capture());
        verifyNoMoreInteractions(petRepository);
    }

    @Test
    @DisplayName("Corner: pet not found for owner -> throws NotFoundException and does not delete")
    void petNotFoundForOwner() {
        String petId = "pet-1";
        String ownerId = "owner-2";
        when(petRepository.findByIdAndOwner_Id(petId, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(petId, ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pet not found");

        verify(petRepository, never()).delete(any());
    }
}
