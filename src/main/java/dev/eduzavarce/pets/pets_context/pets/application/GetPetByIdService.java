package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.PetWithOwnerDto;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import dev.eduzavarce.pets.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPetByIdService {
    private final PetRepository petRepository;

    public GetPetByIdService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public PetWithOwnerDto execute(String petId, String ownerId) {
        PetPostgresEntity p = petRepository.findByIdAndOwner_Id(petId, ownerId)
                .orElseThrow(() -> new NotFoundException("Pet not found"));

        var domain = p.toDomain();
        return new PetWithOwnerDto(
                p.getId(),
                domain.getName(),
                p.getOwner().getId(),
                p.getOwner().getUsername(),
                domain.getHealth(),
                domain.getHunger(),
                domain.getStamina(),
                domain.getType()
        );
    }
}
