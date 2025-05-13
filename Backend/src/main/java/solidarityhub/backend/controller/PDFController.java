package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solidarityhub.backend.repository.PDFCertificateRepository;

@RequestMapping("/solidarityhub/pdf")
@RestController
public class PDFController {
    private final PDFCertificateRepository documentoPDFRepository;

    @Autowired
    public PDFController(PDFCertificateRepository documentoPDFRepository) {
        this.documentoPDFRepository = documentoPDFRepository;
    }
 
    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok().build();
    }
}