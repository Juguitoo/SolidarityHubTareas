package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solidarityhub.backend.model.PDFCertificate;

@Repository
public interface PDFCertificateRepository extends JpaRepository<PDFCertificate, Long> {
}
