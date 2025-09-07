package dev.eduzavarce.pets.auth.users.application;

import dev.eduzavarce.pets.auth.users.domain.CreateUserDto;
import dev.eduzavarce.pets.auth.users.domain.PasswordHasher;
import dev.eduzavarce.pets.auth.users.domain.User;
import dev.eduzavarce.pets.auth.users.infrastructure.AuthUserRepository;
import dev.eduzavarce.pets.auth.users.infrastructure.CreateUserRequest;
import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.shared.core.domain.EventBus;
import dev.eduzavarce.pets.shared.exceptions.AlreadyExistsException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CreateUserService {
    private final PasswordHasher passwordHasher;
    private final AuthUserRepository authUserRepository;
    private final EventBus eventBus;

    public CreateUserService(PasswordHasher passwordHasher, AuthUserRepository authUserRepository, EventBus eventBus) {
        this.authUserRepository = authUserRepository;
        this.passwordHasher = passwordHasher;
        this.eventBus = eventBus;
    }

    public void createUser(CreateUserRequest createUserRequest) {
        String hashedPassword = passwordHasher.hash(createUserRequest.password());
        CreateUserDto createUserDto = new CreateUserDto(
                createUserRequest.id(),
                createUserRequest.username(),
                createUserRequest.email(),
                hashedPassword
        );
        ensureUserDosNotExist(createUserDto);

        User user = User.create(createUserDto);
        authUserRepository.save(new UserPostgresEntity(createUserDto));
        eventBus.publish(user.pullDomainEvents());
    }

    private void ensureUserDosNotExist(CreateUserDto createUserDto) {
        Optional<UserPostgresEntity> existingUser = authUserRepository.findById(createUserDto.id());
        if (existingUser.isPresent()) {
            throw new AlreadyExistsException("User with ID " + createUserDto.id() + " already exists.");
        }
        Optional<UserPostgresEntity> existingEmail = authUserRepository.findByEmail(createUserDto.email());
        if (existingEmail.isPresent()) {
            throw new AlreadyExistsException("User with email " + createUserDto.email() + " already exists.");

        }
    }
}
