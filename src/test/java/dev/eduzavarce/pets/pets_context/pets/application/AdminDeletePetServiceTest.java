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
class AdminDeletePetServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    AdminDeletePetService service;

    @Captor
    ArgumentCaptor<PetPostgresEntity> entityCaptor;

    @Test
    @DisplayName("Happy path: deletes existing pet by id")
    void deletesExistingPet() {
        String petId = "pet-123";
        PetPostgresEntity entity = mock(PetPostgresEntity.class);
        when(petRepository.findById(petId)).thenReturn(Optional.of(entity));

        service.execute(petId);

        verify(petRepository).delete(entityCaptor.capture());
        verifyNoMoreInteractions(petRepository);
    }

    @Test
    @DisplayName("Corner: pet not found -> throws NotFoundException and does not call delete")
    void petNotFound() {
        String petId = "missing";
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(petId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pet not found");

        verify(petRepository, never()).delete(any());
    }
}
