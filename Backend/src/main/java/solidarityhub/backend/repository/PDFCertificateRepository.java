package solidarityhub.backend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import solidarityhub.backend.model.PDFCertificate;

@Repository
public interface PDFCertificateRepository extends JpaRepository<PDFCertificate, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PDFCertificate p WHERE p.id = :id")
    void deleteById(@Param("id")int id);
}
