package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserRepository;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePetServiceTest {

    @Mock
    PetRepository petRepository;
    @Mock
    PetsUserRepository petsUserRepository;

    @InjectMocks
    CreatePetService service;

    @Captor
    ArgumentCaptor<PetPostgresEntity> entityCaptor;

    String id;
    String ownerId;
    String name;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID().toString();
        ownerId = UUID.randomUUID().toString();
        name = "Fluffy";
    }

    @Test
    @DisplayName("Happy path: creates domain Pet, verifies owner exists, saves entity, returns domain Pet")
    void happyPath() {
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        when(petsUserRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Pet pet = service.execute(id, name, ownerId, PetType.DOG);

        assertThat(pet.getId()).isEqualTo(id);
        assertThat(pet.getName()).isEqualTo(name);
        assertThat(pet.getOwnerId()).isEqualTo(ownerId);
        assertThat(pet.getType()).isEqualTo(PetType.DOG);

        verify(petRepository).save(entityCaptor.capture());
        PetPostgresEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        // owner is the same instance we fetched
        assertThat(saved.getOwner()).isSameAs(owner);
    }

    @Test
    @DisplayName("Owner not found: throws NotFoundException and does not save")
    void ownerNotFound() {
        when(petsUserRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id, name, ownerId, PetType.CAT))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner (pets user) not found");

        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Repository.save throws: propagate and do not swallow errors")
    void repositorySaveThrows() {
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        when(petsUserRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        doThrow(new RuntimeException("db error")).when(petRepository).save(any(PetPostgresEntity.class));

        assertThatThrownBy(() -> service.execute(id, name, ownerId, PetType.CANARY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");
    }

    @Test
    @DisplayName("Domain defaults: health/hunger/stamina initialized to 50 as per service")
    void domainDefaults() {
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        when(petsUserRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Pet pet = service.execute(id, name, ownerId, PetType.CAT);

        assertThat(pet.getHealth()).isEqualTo(50);
        assertThat(pet.getHunger()).isEqualTo(50);
        assertThat(pet.getStamina()).isEqualTo(50);
    }
}
