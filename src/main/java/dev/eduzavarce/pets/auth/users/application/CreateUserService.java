package dev.eduzavarce.pets.auth.users.application;

import dev.eduzavarce.pets.auth.users.domain.CreateUserDto;
import dev.eduzavarce.pets.auth.users.domain.PasswordHasher;
import dev.eduzavarce.pets.auth.users.domain.User;
import dev.eduzavarce.pets.auth.users.infrastructure.AuthUserRepository;
import dev.eduzavarce.pets.auth.users.infrastructure.CreateUserRequest;
import dev.eduzavarce.pets.auth.users.infrastructure.UserPostgresEntity;
import dev.eduzavarce.pets.shared.core.domain.EventBus;
import dev.eduzavarce.pets.shared.exceptions.AlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CreateUserService {
    private static final Logger log = LoggerFactory.getLogger(CreateUserService.class);

    private final PasswordHasher passwordHasher;
    private final AuthUserRepository authUserRepository;
    private final EventBus eventBus;

    public CreateUserService(PasswordHasher passwordHasher, AuthUserRepository authUserRepository, EventBus eventBus) {
        this.authUserRepository = authUserRepository;
        this.passwordHasher = passwordHasher;
        this.eventBus = eventBus;
    }

    public void createUser(CreateUserRequest createUserRequest) {
        log.info("[CreateUser] Starting user creation for id={} and email={}", createUserRequest.id(), createUserRequest.email());
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
        log.info("[CreateUser] User created and events published for id={} email={}", createUserDto.id(), createUserDto.email());
    }

    private void ensureUserDosNotExist(CreateUserDto createUserDto) {
        Optional<UserPostgresEntity> existingUser = authUserRepository.findById(createUserDto.id());
        if (existingUser.isPresent()) {
            log.warn("[CreateUser] Attempt to create user with existing id={}", createUserDto.id());
            throw new AlreadyExistsException("User with ID " + createUserDto.id() + " already exists.");
        }
        Optional<UserPostgresEntity> existingEmail = authUserRepository.findByEmail(createUserDto.email());
        if (existingEmail.isPresent()) {
            log.warn("[CreateUser] Attempt to create user with existing email={}", createUserDto.email());
            throw new AlreadyExistsException("User with email " + createUserDto.email() + " already exists.");

        }
    }
}
