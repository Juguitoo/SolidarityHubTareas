package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solidarityhub.backend.model.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {

}
