package dev.eduzavarce.pets.auth.users.infrastructure;

import dev.eduzavarce.pets.auth.users.domain.CreateUserDto;
import dev.eduzavarce.pets.auth.users.domain.User;
import dev.eduzavarce.pets.auth.users.domain.UserDto;
import dev.eduzavarce.pets.auth.users.domain.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Entity(name = "auth_users")
public class UserPostgresEntity implements UserEntity, UserDetails {
    @Id
    private String id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public UserPostgresEntity(CreateUserDto createUserDto) {
        this.id = createUserDto.id();
        this.username = createUserDto.username();
        this.email = createUserDto.email();
        this.password = createUserDto.password();
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    protected UserPostgresEntity() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public User toDomain() {
        return User.fromPrimitives(new UserDto(id, username, email, password));
    }
}
