package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import solidarityhub.backend.model.Need;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NeedRepository extends JpaRepository<Need, Integer> {
    @Query("SELECT n FROM Need n WHERE n.task.id IS NULL AND n.catastrophe.id = :catastropheid")
    public List<Need> getNeedsWithoutTask(@Param("catastropheid") Integer id);

    @Query("SELECT n FROM Need n WHERE n.catastrophe.id = :catastropheid")
    public List<Need> getAllNeeds(@Param("catastropheid") Integer id);

    @Query("SELECT COUNT(n) FROM Need n WHERE n.task.id IS NULL AND n.catastrophe.id = :catastropheid")
    public int getNeedWithoutTaskCount(@Param("catastropheid") Integer id);
}
