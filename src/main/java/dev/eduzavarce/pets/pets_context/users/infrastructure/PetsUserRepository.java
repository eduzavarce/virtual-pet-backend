package dev.eduzavarce.pets.pets_context.users.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PetsUserRepository extends JpaRepository<PetsUserPostgresEntity, String> {

}
