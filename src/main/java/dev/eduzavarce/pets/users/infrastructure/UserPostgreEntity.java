package dev.eduzavarce.pets.users.infrastructure;

import dev.eduzavarce.pets.users.domain.CreateUserDto;
import dev.eduzavarce.pets.users.domain.User;
import dev.eduzavarce.pets.users.domain.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity(name = "users")
public class UserPostgreEntity implements UserEntity {
    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private String role;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public UserPostgreEntity(CreateUserDto createUserDto) {
        this.id = createUserDto.id();
        this.username = createUserDto.username();
        this.email = createUserDto.email();
        this.password = createUserDto.password();
        this.role = createUserDto.role();
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
    protected UserPostgreEntity() {}

    @Override
    public User toDomain() {
        return null;
    }
}
