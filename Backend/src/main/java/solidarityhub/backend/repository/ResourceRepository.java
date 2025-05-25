package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.enums.ResourceType;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Integer> {

    @Query("SELECT r FROM Resource r WHERE r.catastrophe.id = :catastropheid")
    List<Resource> getResourcesByCatastrophe(@Param ("catastropheid") int catastropheId);

    @Query("SELECT COUNT(r.ID) FROM Resource r WHERE r.type = :resourceType AND r.catastrophe.id = :catastropheId")
    Integer getCountByType(@Param("resourceType") ResourceType resourceType, @Param ("catastropheId") Integer catastropheId);

    @Query("SELECT SUM(r.quantity) FROM Resource r WHERE r.type = :resourceType AND r.catastrophe.id = :catastropheId")
    Double getTotalQuantityByType(@Param("resourceType") ResourceType resourceType, @Param ("catastropheId") Integer catastropheId);
}
