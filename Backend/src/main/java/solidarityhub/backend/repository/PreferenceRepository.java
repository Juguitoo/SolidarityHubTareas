package solidarityhub.backend.repository;

import solidarityhub.backend.model.Preference;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PreferenceRepository extends JpaRepository<Preference, Integer> {
}
