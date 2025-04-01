package solidarityhub.backend.repository;

import solidarityhub.backend.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PersonRepository extends JpaRepository<Person, String> {
}
