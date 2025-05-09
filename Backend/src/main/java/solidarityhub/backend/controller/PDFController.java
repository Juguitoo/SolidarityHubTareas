package solidarityhub.backend.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solidarityhub.backend.model.PDFDocument;
import solidarityhub.backend.repository.PDFDocumentRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        try (PDDocument document = new PDDocument()) {
            // Crear una página
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Comenzar a escribir contenido
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Hola mundo desde PDFBox!");
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            PDFDocument doc = new PDFDocument("ejemplo.pdf", outputStream.toByteArray());
            documentoPDFRepository.save(doc);
            document.save("ejemploAntes.pdf");
            PDFDocument bd = documentoPDFRepository.findById(3L).get();
            try (PDDocument loadedDocument = PDDocument.load(new ByteArrayInputStream(bd.getContenido()))) {
                loadedDocument.save("ejemploDsps.pdf");
                System.out.println("PDF cargado correctamente desde la base de datos.");
            }

            System.out.println("PDF creado correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();

    }
}