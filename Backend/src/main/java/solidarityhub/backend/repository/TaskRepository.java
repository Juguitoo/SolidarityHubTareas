package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import solidarityhub.backend.model.enums.Status;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT new solidarityhub.backend.dto.TaskDTO(t) FROM Task t WHERE t.catastrophe.id = :catastropheId")
    List<TaskDTO> findAllByCatastropheId(@Param("catastropheId") Integer catastropheId);

    @Query("SELECT COUNT(t.id) FROM Task t WHERE t.catastrophe.id = :catastropheId AND t.status = :status")
    Integer getTasksByStatusCount(@Param("catastropheId") Integer catastropheId, @Param("status") Status status);

    @Query("SELECT t FROM Task t WHERE t.catastrophe.id = :catastropheId")
    List<Task> getTasksByCatastrophe(@Param("catastropheId") Integer catastropheId);

    @Modifying
    @Query(value = "UPDATE task SET status = ?2 WHERE id = ?1", nativeQuery = true)
    int updateTaskStatus(int taskId, String status);
}
