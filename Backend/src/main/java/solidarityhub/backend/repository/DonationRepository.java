package solidarityhub.backend.repository;

import solidarityhub.backend.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DonationRepository extends JpaRepository<Donation, Integer> {
}
