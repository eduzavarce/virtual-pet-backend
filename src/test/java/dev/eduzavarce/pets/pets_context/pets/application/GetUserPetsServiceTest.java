package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.domain.PetDto;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.pets_context.pets.domain.PetWithOwnerDto;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserPetsServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    GetUserPetsService service;

    @Captor
    ArgumentCaptor<String> userIdCaptor;

    @Test
    @DisplayName("Happy path: maps list of PetPostgresEntity to PetWithOwnerDto preserving repository ordering")
    void mapsAndPreservesOrder() {
        String userId = "11111111-1111-1111-1111-111111111111";

        // Prepare owners
        PetsUserPostgresEntity owner = mock(PetsUserPostgresEntity.class);
        when(owner.getId()).thenReturn(userId);
        when(owner.getUsername()).thenReturn("johnny");

        // Prepare two pets with descending created order simulated by list order
        PetDto dto1 = new PetDto("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2", "Beta", userId, 80, 20, 60, PetType.RABBIT);
        PetDto dto0 = new PetDto("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1", "Alpha", userId, 70, 30, 90, PetType.DOG);
        Pet domain1 = Pet.fromPrimitives(dto1);
        Pet domain0 = Pet.fromPrimitives(dto0);

        PetPostgresEntity e1 = mock(PetPostgresEntity.class);
        when(e1.getId()).thenReturn("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2");
        when(e1.getOwner()).thenReturn(owner);
        when(e1.toDomain()).thenReturn(domain1);

        PetPostgresEntity e0 = mock(PetPostgresEntity.class);
        when(e0.getId()).thenReturn("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
        when(e0.getOwner()).thenReturn(owner);
        when(e0.toDomain()).thenReturn(domain0);

        // Repository returns in the expected (createdAt desc) order: e1, e0
        when(petRepository.findByOwner_IdOrderByCreatedAtDesc(userId)).thenReturn(List.of(e1, e0));

        // Act
        List<PetWithOwnerDto> result = service.execute(userId);

        // Assert size and order
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2");
        assertThat(result.get(1).id()).isEqualTo("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");

        // Assert mapping for first
        PetWithOwnerDto r0 = result.get(0);
        assertThat(r0.name()).isEqualTo("Beta");
        assertThat(r0.ownerId()).isEqualTo(userId);
        assertThat(r0.ownerUsername()).isEqualTo("johnny");
        assertThat(r0.health()).isEqualTo(80);
        assertThat(r0.hunger()).isEqualTo(20);
        assertThat(r0.stamina()).isEqualTo(60);
        assertThat(r0.type()).isEqualTo(PetType.RABBIT);

        // Assert mapping for second
        PetWithOwnerDto r1 = result.get(1);
        assertThat(r1.name()).isEqualTo("Alpha");
        assertThat(r1.ownerId()).isEqualTo(userId);
        assertThat(r1.ownerUsername()).isEqualTo("johnny");
        assertThat(r1.health()).isEqualTo(70);
        assertThat(r1.hunger()).isEqualTo(30);
        assertThat(r1.stamina()).isEqualTo(90);
        assertThat(r1.type()).isEqualTo(PetType.DOG);

        // Verify repository interaction
        verify(petRepository).findByOwner_IdOrderByCreatedAtDesc(userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        verifyNoMoreInteractions(petRepository);

        // Ensure per-entity domain mapping was requested
        verify(e1, atLeastOnce()).toDomain();
        verify(e0, atLeastOnce()).toDomain();
    }

    @Test
    @DisplayName("Empty list: returns empty and only queries repository once")
    void emptyListReturnsEmpty() {
        String userId = "22222222-2222-2222-2222-222222222222";
        when(petRepository.findByOwner_IdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        List<PetWithOwnerDto> result = service.execute(userId);

        assertThat(result).isEmpty();
        verify(petRepository).findByOwner_IdOrderByCreatedAtDesc(userId);
        verifyNoMoreInteractions(petRepository);
    }
}
