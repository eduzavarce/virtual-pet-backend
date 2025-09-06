package dev.eduzavarce.pets.auth.users.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<UserPostgresEntity, String> {

    Optional<UserPostgresEntity> findByEmail(String email);
}
