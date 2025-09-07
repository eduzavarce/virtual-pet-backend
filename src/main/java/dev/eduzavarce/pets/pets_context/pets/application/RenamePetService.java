package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RenamePetService {
    private final PetRepository petRepository;

    public RenamePetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional
    public void execute(String petId, String ownerId, String newName) {
        PetPostgresEntity entity = petRepository.findByIdAndOwner_Id(petId, ownerId)
                .orElseThrow(() -> new NotFoundException("Pet not found"));

        Pet current = entity.toDomain();
        current.rename(newName);


        PetPostgresEntity updatedEntity = new PetPostgresEntity(current, entity.getOwner());


        petRepository.save(updatedEntity);
    }
}
