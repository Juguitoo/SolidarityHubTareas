package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.model.ResourceAssignment;

import java.util.List;

public interface ResourceAssignmentRepository extends JpaRepository<ResourceAssignment, Integer> {
    List<ResourceAssignment> findByTaskId(int taskId);

    List<ResourceAssignment> findByResourceId(int resourceId);

    @Query("SELECT COALESCE(SUM(ra.quantity), 0) FROM ResourceAssignment ra WHERE ra.resource.id = :resourceId")
    Double getTotalAssignedQuantity(@Param("resourceId") int resourceId);
}
