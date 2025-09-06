package dev.eduzavarce.pets.users.application;

import dev.eduzavarce.pets.shared.exceptions.AlreadyExistsException;
import dev.eduzavarce.pets.users.domain.*;
import dev.eduzavarce.pets.users.infrastructure.CreateUserRequest;
import dev.eduzavarce.pets.users.infrastructure.UserPostgreEntity;
import dev.eduzavarce.pets.users.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CreateUserService {
    private final PasswordHasher passwordHasher;
    private final UserRepository userRepository;


    public CreateUserService(PasswordHasher passwordHasher, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }
    public void createUser(CreateUserRequest createUserRequest) {
        String hashedPassword = passwordHasher.hash(createUserRequest.password());
        CreateUserDto createUserDto = new CreateUserDto(
                createUserRequest.id(),
                createUserRequest.username(),
                createUserRequest.email(),
                hashedPassword,
                "ROLE_USER"
        );
        ensureUserDosNotExist(createUserDto);

        User user = User.create(createUserDto);
        userRepository.save(new UserPostgreEntity(createUserDto));

    }

    private void ensureUserDosNotExist(CreateUserDto createUserDto) {
        Optional<UserPostgreEntity> existingUser = userRepository.findById(createUserDto.id());
        if (existingUser.isPresent()) {
            throw new AlreadyExistsException("User with ID " + createUserDto.id() + " already exists.");
        }
        Optional<UserPostgreEntity> existingEmail = userRepository.findByEmail(createUserDto.email());
        if (existingEmail.isPresent()) {
            throw new AlreadyExistsException("User with email " + createUserDto.email() + " already exists.");

        }
    }
}
