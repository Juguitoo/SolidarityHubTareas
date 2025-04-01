package solidarityhub.backend.repository;

import solidarityhub.backend.model.Affected;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectedRepository extends JpaRepository<Affected, String> {
}
