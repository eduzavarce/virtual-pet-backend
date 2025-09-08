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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAllPetsServiceTest {

    @Mock
    PetRepository petRepository;
    @InjectMocks
    ListAllPetsService service;

    @Test
    @DisplayName("Happy path: maps all PetPostgresEntity to PetWithOwnerDto preserving repository ordering")
    void mapsAllAndPreservesOrder() {
        // Arrange owner
        PetsUserPostgresEntity ownerA = mock(PetsUserPostgresEntity.class);
        when(ownerA.getId()).thenReturn("11111111-1111-1111-1111-111111111111");
        when(ownerA.getUsername()).thenReturn("johnny");

        // Prepare two pets returned by repo in desc order
        PetDto dto1 = new PetDto("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2", "Beta", ownerA.getId(), 80, 20, 60, PetType.RABBIT);
        PetDto dto0 = new PetDto("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1", "Alpha", ownerA.getId(), 70, 30, 90, PetType.DOG);
        Pet domain1 = Pet.fromPrimitives(dto1);
        Pet domain0 = Pet.fromPrimitives(dto0);

        PetPostgresEntity e1 = mock(PetPostgresEntity.class);
        when(e1.getId()).thenReturn("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2");
        when(e1.getOwner()).thenReturn(ownerA);
        when(e1.toDomain()).thenReturn(domain1);

        PetPostgresEntity e0 = mock(PetPostgresEntity.class);
        when(e0.getId()).thenReturn("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
        when(e0.getOwner()).thenReturn(ownerA);
        when(e0.toDomain()).thenReturn(domain0);

        when(petRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(e1, e0));

        // Act
        List<PetWithOwnerDto> result = service.execute();

        // Assert size and order
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2");
        assertThat(result.get(1).id()).isEqualTo("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");

        // First mapping
        PetWithOwnerDto r0 = result.get(0);
        assertThat(r0.name()).isEqualTo("Beta");
        assertThat(r0.ownerId()).isEqualTo(ownerA.getId());
        assertThat(r0.ownerUsername()).isEqualTo("johnny");
        assertThat(r0.health()).isEqualTo(80);
        assertThat(r0.hunger()).isEqualTo(20);
        assertThat(r0.stamina()).isEqualTo(60);
        assertThat(r0.type()).isEqualTo(PetType.RABBIT);

        // Second mapping
        PetWithOwnerDto r1 = result.get(1);
        assertThat(r1.name()).isEqualTo("Alpha");
        assertThat(r1.ownerId()).isEqualTo(ownerA.getId());
        assertThat(r1.ownerUsername()).isEqualTo("johnny");
        assertThat(r1.health()).isEqualTo(70);
        assertThat(r1.hunger()).isEqualTo(30);
        assertThat(r1.stamina()).isEqualTo(90);
        assertThat(r1.type()).isEqualTo(PetType.DOG);

        verify(petRepository).findAllByOrderByCreatedAtDesc();
        verifyNoMoreInteractions(petRepository);

        verify(e1, atLeastOnce()).toDomain();
        verify(e0, atLeastOnce()).toDomain();
    }

    @Test
    @DisplayName("Empty list: returns empty collection and only queries repository once")
    void emptyListReturnsEmpty() {
        when(petRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<PetWithOwnerDto> result = service.execute();

        assertThat(result).isEmpty();
        verify(petRepository).findAllByOrderByCreatedAtDesc();
        verifyNoMoreInteractions(petRepository);
    }
}
