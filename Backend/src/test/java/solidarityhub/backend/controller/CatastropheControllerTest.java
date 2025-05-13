package solidarityhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.dto.CatastropheDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.service.CatastropheService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CatastropheControllerTest {

    @Mock
    private CatastropheService catastropheService;

    @InjectMocks
    private CatastropheController catastropheController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCatastrophes() {
        // Arrange
        List<Catastrophe> catastrophes = new ArrayList<>();
        catastrophes.add(createTestCatastrophe(1));
        catastrophes.add(createTestCatastrophe(2));
        when(catastropheService.getAllCatastrophes()).thenReturn(catastrophes);

        // Act
        ResponseEntity<?> response = catastropheController.getCatastrophes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof CatastropheDTO);
        verify(catastropheService, times(1)).getAllCatastrophes();
    }

    @Test
    void testGetCatastrophe_ExistingId() {
        // Arrange
        int catastropheId = 1;
        Catastrophe catastrophe = createTestCatastrophe(catastropheId);
        when(catastropheService.getCatastrophe(catastropheId)).thenReturn(catastrophe);

        // Act
        ResponseEntity<?> response = catastropheController.getCatastrophe(catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Catastrophe);
        Catastrophe responseBody = (Catastrophe) response.getBody();
        assertEquals(catastropheId, responseBody.getId());
        assertEquals("Test Catastrophe 1", responseBody.getName());
        verify(catastropheService, times(1)).getCatastrophe(catastropheId);
    }

    @Test
    void testGetCatastrophe_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(catastropheService.getCatastrophe(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = catastropheController.getCatastrophe(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(catastropheService, times(1)).getCatastrophe(nonExistingId);
    }

    @Test
    void testAddCatastrophe() {
        // Arrange
        CatastropheDTO catastropheDTO = createTestCatastropheDTO();
        Catastrophe savedCatastrophe = createTestCatastrophe(1);
        when(catastropheService.save(any(Catastrophe.class))).thenReturn(savedCatastrophe);

        // Act
        ResponseEntity<?> response = catastropheController.addCatastrophe(catastropheDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(catastropheService, times(1)).save(any(Catastrophe.class));
    }

    @Test
    void testUpdateCatastrophe_ExistingId() {
        // Arrange
        int catastropheId = 1;
        CatastropheDTO catastropheDTO = createTestCatastropheDTO();
        Catastrophe existingCatastrophe = createTestCatastrophe(catastropheId);
        when(catastropheService.getCatastrophe(catastropheId)).thenReturn(existingCatastrophe);
        when(catastropheService.save(any(Catastrophe.class))).thenReturn(existingCatastrophe);

        // Act
        ResponseEntity<?> response = catastropheController.updateCatastrophe(catastropheId, catastropheDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(catastropheService, times(1)).getCatastrophe(catastropheId);
        verify(catastropheService, times(1)).save(any(Catastrophe.class));
    }

    @Test
    void testUpdateCatastrophe_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        CatastropheDTO catastropheDTO = createTestCatastropheDTO();
        when(catastropheService.getCatastrophe(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = catastropheController.updateCatastrophe(nonExistingId, catastropheDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(catastropheService, times(1)).getCatastrophe(nonExistingId);
        verify(catastropheService, never()).save(any(Catastrophe.class));
    }

    @Test
    void testDeleteCatastrophe_ExistingId() {
        // Arrange
        int catastropheId = 1;
        Catastrophe existingCatastrophe = createTestCatastrophe(catastropheId);
        when(catastropheService.getCatastrophe(catastropheId)).thenReturn(existingCatastrophe);
        doNothing().when(catastropheService).deleteCatastrophe(catastropheId);

        // Act
        ResponseEntity<?> response = catastropheController.deleteCatastrophe(catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(catastropheService, times(1)).getCatastrophe(catastropheId);
        verify(catastropheService, times(1)).deleteCatastrophe(catastropheId);
    }

    @Test
    void testDeleteCatastrophe_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(catastropheService.getCatastrophe(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = catastropheController.deleteCatastrophe(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(catastropheService, times(1)).getCatastrophe(nonExistingId);
        verify(catastropheService, never()).deleteCatastrophe(nonExistingId);
    }

    // Helper methods
    private Catastrophe createTestCatastrophe(int id) {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe " + id,
                "Test Description " + id,
                coordinates,
                LocalDate.now(),
                EmergencyLevel.MEDIUM
        );

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return catastrophe;
    }

    private CatastropheDTO createTestCatastropheDTO() {
        CatastropheDTO dto = new CatastropheDTO();
        // Set properties using reflection since there are no setters
        try {
            java.lang.reflect.Field nameField = CatastropheDTO.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(dto, "Test Catastrophe");

            java.lang.reflect.Field descriptionField = CatastropheDTO.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(dto, "Test Description");

            java.lang.reflect.Field locationXField = CatastropheDTO.class.getDeclaredField("locationX");
            locationXField.setAccessible(true);
            locationXField.set(dto, 40.416775);

            java.lang.reflect.Field locationYField = CatastropheDTO.class.getDeclaredField("locationY");
            locationYField.setAccessible(true);
            locationYField.set(dto, -3.703790);

            java.lang.reflect.Field startDateField = CatastropheDTO.class.getDeclaredField("startDate");
            startDateField.setAccessible(true);
            startDateField.set(dto, LocalDate.now());

            java.lang.reflect.Field emergencyLevelField = CatastropheDTO.class.getDeclaredField("emergencyLevel");
            emergencyLevelField.setAccessible(true);
            emergencyLevelField.set(dto, EmergencyLevel.MEDIUM);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }
}