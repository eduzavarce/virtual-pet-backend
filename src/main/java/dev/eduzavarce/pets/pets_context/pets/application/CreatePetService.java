package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.Pet;
import dev.eduzavarce.pets.pets_context.pets.domain.PetDto;
import dev.eduzavarce.pets.pets_context.pets.domain.PetType;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserRepository;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePetService {
    private final PetRepository petRepository;
    private final PetsUserRepository petsUserRepository;

    public CreatePetService(PetRepository petRepository, PetsUserRepository petsUserRepository) {
        this.petRepository = petRepository;
        this.petsUserRepository = petsUserRepository;
    }

    @Transactional
    public Pet execute(String id, String name, String ownerId, PetType type) {
            PetDto dto = new PetDto(id, name, ownerId, 50, 50, 50, type);
            Pet pet = Pet.create(dto);
            PetsUserPostgresEntity owner = petsUserRepository.findById(ownerId)
                    .orElseThrow(() -> new NotFoundException("Owner (pets user) not found: " + ownerId));
            PetPostgresEntity entity = new PetPostgresEntity(pet, owner);
            petRepository.save(entity);
            return pet;
        }
}
