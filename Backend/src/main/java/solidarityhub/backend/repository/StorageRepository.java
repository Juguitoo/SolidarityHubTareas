package solidarityhub.backend.repository;

import solidarityhub.backend.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<Storage, Integer> {
}
