package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import solidarityhub.backend.dto.StorageDTO;
import solidarityhub.backend.model.Storage;

import java.util.List;

public interface StorageRepository extends JpaRepository<Storage, Integer> {

    @Query("SELECT new solidarityhub.backend.dto.StorageDTO(s.id, s.name, s.isFull) FROM Storage s")
    List<StorageDTO> findStorages();
}
