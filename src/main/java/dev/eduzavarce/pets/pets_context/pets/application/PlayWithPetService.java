package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayWithPetService {
    private final PetRepository petRepository;

    public PlayWithPetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional
    public Pet execute(String petId, String ownerId) {
        PetPostgresEntity entity = petRepository.findByIdAndOwner_Id(petId, ownerId)
                .orElseThrow(() -> new NotFoundException("Pet not found"));

        Pet pet = entity.toDomain();
        pet.play();

        PetPostgresEntity updated = new PetPostgresEntity(pet, entity.getOwner());
        petRepository.save(updated);

        return pet;
    }
}
