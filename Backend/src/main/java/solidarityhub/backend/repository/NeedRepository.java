package solidarityhub.backend.repository;

import solidarityhub.backend.model.Need;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NeedRepository extends JpaRepository<Need, Integer> {
}
