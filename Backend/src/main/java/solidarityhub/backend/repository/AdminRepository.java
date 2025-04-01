package solidarityhub.backend.repository;
import solidarityhub.backend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AdminRepository extends JpaRepository<Admin, String> {
}
