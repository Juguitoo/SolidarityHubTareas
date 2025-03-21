package solidarityhub.backend.repository;

import solidarityhub.backend.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ResourceRepository extends JpaRepository<Resource, Integer> {
}
