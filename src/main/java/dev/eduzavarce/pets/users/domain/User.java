package dev.eduzavarce.pets.users.domain;

import dev.eduzavarce.pets.shared.core.domain.AggregateRoot;

public class User extends AggregateRoot {
    private final UserId id;
    private final Username username;
    private final UserEmail email;
    private final UserPassword password;
    private final UserRole role;

    private User(UserId id, Username username, UserEmail email, UserPassword password, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User create(CreateUserDto createUserDto) {
        final UserId userId = new UserId(createUserDto.id());
        final Username username = new Username(createUserDto.username());
        final UserEmail email = new UserEmail(createUserDto.email());
        final UserPassword password = new UserPassword(createUserDto.password());
        final UserRole role = UserRole.ROLE_USER;
        User createdUser = new User(userId, username, email, password, role);
        createdUser.record(
                new UserCreated(createdUser.id.value(), "user.created", createdUser.toPrimitives()));
        return createdUser;
    }



    public UserDto toPrimitives() {
        return new UserDto(id.value(), username.value(), email.value(), role.name());
    }
//    public User

}
