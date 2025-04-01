package solidarityhub.backend.repository;

import solidarityhub.backend.model.Catastrophe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatastropheRepository extends JpaRepository<Catastrophe, Integer> {
}
