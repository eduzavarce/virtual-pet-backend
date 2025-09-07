package dev.eduzavarce.pets.pets_context.users.infrastructure;

import dev.eduzavarce.pets.pets_context.pets.infrastructure.PetPostgresEntity;
import dev.eduzavarce.pets.pets_context.users.domain.PetUser;
import dev.eduzavarce.pets.pets_context.users.domain.PetUserDto;
import dev.eduzavarce.pets.pets_context.users.domain.PetsUserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity(name = "pets_users")
public class PetsUserPostgresEntity extends PetsUserEntity {
    @Id
    private String id;
    @Column(unique = true, nullable = false)
    private String username;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetPostgresEntity> pets;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    protected PetsUserPostgresEntity() {
    }

    private PetsUserPostgresEntity(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public PetsUserPostgresEntity(PetUserDto petUser) {
        this.id = petUser.id();
        this.username = petUser.username();
    }

    @Override
    public PetUser toDomain() {
        return PetUser.fromPrimitives(new PetUserDto(id, username));
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PetsUserPostgresEntity that = (PetsUserPostgresEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
