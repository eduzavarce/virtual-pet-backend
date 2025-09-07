package dev.eduzavarce.pets.pets_context.pets.application;

import dev.eduzavarce.pets.pets_context.pets.domain.PetWithOwnerDto;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetUserPetsService {
    private final PetRepository petRepository;

    public GetUserPetsService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public List<PetWithOwnerDto> execute(String userId) {
        List<PetPostgresEntity> pets = petRepository.findByOwner_IdOrderByCreatedAtDesc(userId);
        return pets.stream().map(p -> {
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
        }).toList();
    }
}
