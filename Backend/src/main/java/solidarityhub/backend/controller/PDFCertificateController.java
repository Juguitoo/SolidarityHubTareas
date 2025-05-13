package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solidarityhub.backend.repository.PDFCertificateRepository;
import solidarityhub.backend.service.PDFCertificateService;

@RequestMapping("/solidarityhub/pdf")
@RestController
public class PDFCertificateController {
    private final PDFCertificateRepository documentoPDFRepository;
    private final PDFCertificateService pdfService;

    @Autowired
    public PDFCertificateController(PDFCertificateRepository documentoPDFRepository, PDFCertificateService pdfService) {
        this.documentoPDFRepository = documentoPDFRepository;
        this.pdfService = pdfService;
    }
 
    @GetMapping
    public ResponseEntity<?> get() {
        pdfService.createGenericPDFDocument();
        return ResponseEntity.ok().build();
    }
}