package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.dto.ResourceQuantityDTO;
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

    @Query("SELECT new solidarityhub.backend.dto.ResourceQuantityDTO(new solidarityhub.backend.dto.ResourceDTO(r) , (r.quantity - COALESCE(SUM(ra.quantity), 0))) " +
            "FROM solidarityhub.backend.model.Resource r LEFT JOIN r.resourceAssignments ra " +
            "WHERE r.catastrophe.id = :catastropheId " +
            "GROUP BY r.id, r.quantity, ra.quantity, r.catastrophe.id")
    List<ResourceQuantityDTO> getResourcesAndAvailableQuantity(@Param("catastropheId") Integer catastropheId);
}
