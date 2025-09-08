package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeletePetService {
    private final PetRepository petRepository;

    public DeletePetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional
    public void execute(String petId, String ownerId) {
        PetPostgresEntity entity = petRepository.findByIdAndOwner_Id(petId, ownerId)
                .orElseThrow(() -> new NotFoundException("Pet not found"));
        petRepository.delete(entity);
    }
}
