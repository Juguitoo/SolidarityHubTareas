package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.model.Resource;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {

    @Query("SELECT r FROM Resource r WHERE r.catastrophe.id = :catastropheid")
    List<Resource> getResourcesByCatastrophe(@Param ("catastropheid") int catastropheId);
}
