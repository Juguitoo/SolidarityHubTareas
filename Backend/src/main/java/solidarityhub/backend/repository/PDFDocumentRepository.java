package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solidarityhub.backend.model.PDFDocument;

@Repository
public interface PDFDocumentRepository extends JpaRepository<PDFDocument, Long> {
}
