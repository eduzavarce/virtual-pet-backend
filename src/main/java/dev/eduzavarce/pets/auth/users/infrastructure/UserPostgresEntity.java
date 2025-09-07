package dev.eduzavarce.pets.auth.users.infrastructure;

import dev.eduzavarce.pets.auth.users.domain.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
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
        this.role = UserRole.ROLE_USER;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    protected UserPostgresEntity() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getId() {
        return id;
    }

    @Override
    public User toDomain() {
        return User.fromPrimitives(new UserDto(id, username, email, role.name()));
    }
}
