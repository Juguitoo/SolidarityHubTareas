package solidarityhub.backend.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solidarityhub.backend.model.PDFDocument;
import solidarityhub.backend.repository.PDFDocumentRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@RequestMapping("/solidarityhub/pdf")
@RestController
public class PDFController {
    private final PDFDocumentRepository documentoPDFRepository;

    @Autowired
    public PDFController(PDFDocumentRepository documentoPDFRepository) {
        this.documentoPDFRepository = documentoPDFRepository;
    }
 
    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok().build();

    }
}