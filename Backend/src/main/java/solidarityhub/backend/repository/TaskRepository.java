package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT new solidarityhub.backend.dto.TaskDTO(t) FROM Task t WHERE t.catastrophe.id = :catastropheId")
    List<TaskDTO> findAllByCatastropheId(@Param("catastropheId") Integer catastropheId);
}
