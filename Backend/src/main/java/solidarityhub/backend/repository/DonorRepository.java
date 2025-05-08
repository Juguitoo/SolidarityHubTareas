package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solidarityhub.backend.model.Donor;

@Repository
public interface DonorRepository extends JpaRepository<Donor, String> {
}