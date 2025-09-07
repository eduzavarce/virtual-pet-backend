package dev.eduzavarce.pets.pets_context.pets.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<PetPostgresEntity, String> {
    List<PetPostgresEntity> findByOwner_Id(String ownerId);
}
