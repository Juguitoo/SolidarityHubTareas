package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.model.ResourceAssignment;

import java.util.List;
import java.util.Map;

public interface ResourceAssignmentRepository extends JpaRepository<ResourceAssignment, Integer> {
    List<ResourceAssignment> findByTaskId(int taskId);

    List<ResourceAssignment> findByResourceId(int resourceId);

    @Query("SELECT COALESCE(SUM(ra.quantity), 0) FROM ResourceAssignment ra WHERE ra.resource.id = :resourceId")
    Double getTotalAssignedQuantity(@Param("resourceId") int resourceId);

    @Query("SELECT r.type AS resourceType, SUM(ra.quantity) AS totalAssigned " +
            "FROM ResourceAssignment ra JOIN ra.resource r " +
            "WHERE r.catastrophe.id = :catastropheId " +
            "GROUP BY r.type")
    List<Map<String, Object>> getAssignedResourcesSummary( @Param("catastropheId") Integer catastropheId);
}
