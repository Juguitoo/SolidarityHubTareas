package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solidarityhub.backend.model.Storage;

public interface StorageRepository extends JpaRepository<Storage, Integer> {
}
