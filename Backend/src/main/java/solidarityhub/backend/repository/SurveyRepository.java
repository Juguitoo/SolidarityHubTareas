package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solidarityhub.backend.model.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Integer> {
}
