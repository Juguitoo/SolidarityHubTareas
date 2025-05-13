package solidarityhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.repository.PDFCertificateRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PDFControllerTest {

    @Mock
    private PDFCertificateRepository documentoPDFRepository;

    @InjectMocks
    private PDFController pdfController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGet() {
        // Act
        ResponseEntity<?> response = pdfController.get();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Since the controller method just returns an empty response with OK status,
        // there's not much else to verify
    }
}