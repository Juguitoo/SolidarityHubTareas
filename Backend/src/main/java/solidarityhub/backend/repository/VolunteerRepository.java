package solidarityhub.backend.repository;

import solidarityhub.backend.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, String> {
}
