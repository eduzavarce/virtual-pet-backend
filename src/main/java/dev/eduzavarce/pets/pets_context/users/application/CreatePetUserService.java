package dev.eduzavarce.pets.pets_context.users.application;

import dev.eduzavarce.pets.auth.users.domain.UserDto;
import dev.eduzavarce.pets.pets_context.users.domain.PetUser;
import dev.eduzavarce.pets.pets_context.users.domain.PetUserDto;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserPostgresEntity;
import dev.eduzavarce.pets.pets_context.users.infrastructure.PetsUserRepository;
import dev.eduzavarce.pets.shared.core.domain.EventBus;
import org.springframework.stereotype.Service;

@Service
public class CreatePetUserService {
    private final PetsUserRepository petsUserRepository;
    private final EventBus eventBus;

    public CreatePetUserService(PetsUserRepository petsUserRepository, EventBus eventBus) {
        this.eventBus = eventBus;
        this.petsUserRepository = petsUserRepository;
    }

    public void execute(UserDto body) {
        PetUserDto petUserDto = new PetUserDto(
                body.id(),
                body.username()
        );
        PetUser petUser = PetUser.create(petUserDto);
        System.out.println("Created pet user: " + petUser);

        petsUserRepository.save(new PetsUserPostgresEntity(petUserDto));
        eventBus.publish(petUser.pullDomainEvents());
    }
}
