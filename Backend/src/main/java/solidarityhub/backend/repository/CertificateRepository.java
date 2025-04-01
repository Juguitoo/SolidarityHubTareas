package solidarityhub.backend.repository;

import solidarityhub.backend.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CertificateRepository extends JpaRepository<Certificate, Integer> {
}
