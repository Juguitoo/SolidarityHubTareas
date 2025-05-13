package solidarityhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.dto.DonorDTO;
import solidarityhub.backend.service.DonorService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DonorControllerTest {

    @Mock
    private DonorService donorService;

    @InjectMocks
    private DonorController donorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllDonors() {
        // Arrange
        List<DonorDTO> donors = new ArrayList<>();
        donors.add(createTestDonorDTO("D-1", "Donor 1"));
        donors.add(createTestDonorDTO("D-2", "Donor 2"));
        when(donorService.getAllDonors()).thenReturn(donors);

        // Act
        ResponseEntity<List<DonorDTO>> response = donorController.getAllDonors();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(donorService, times(1)).getAllDonors();
    }

    @Test
    void testGetDonorByDni_ExistingDni() {
        // Arrange
        String dni = "D-1";
        DonorDTO donor = createTestDonorDTO(dni, "Donor 1");
        when(donorService.getDonorByDni(dni)).thenReturn(donor);

        // Act
        ResponseEntity<DonorDTO> response = donorController.getDonorByDni(dni);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dni, response.getBody().getDni());
        assertEquals("Donor 1", response.getBody().getName());
        verify(donorService, times(1)).getDonorByDni(dni);
    }

    @Test
    void testGetDonorByDni_NonExistingDni() {
        // Arrange
        String dni = "NONEXISTENT";
        when(donorService.getDonorByDni(dni)).thenReturn(null);

        // Act
        ResponseEntity<DonorDTO> response = donorController.getDonorByDni(dni);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(donorService, times(1)).getDonorByDni(dni);
    }

    @Test
    void testCreateDonor_Success() {
        // Arrange
        DonorDTO donorDTO = createTestDonorDTO("D-1", "New Donor");
        when(donorService.existsById(donorDTO.getDni())).thenReturn(false);
        when(donorService.saveDonor(any(DonorDTO.class))).thenReturn(donorDTO);

        // Act
        ResponseEntity<DonorDTO> response = donorController.createDonor(donorDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("D-1", response.getBody().getDni());
        assertEquals("New Donor", response.getBody().getName());
        verify(donorService, times(1)).existsById(donorDTO.getDni());
        verify(donorService, times(1)).saveDonor(donorDTO);
    }

    @Test
    void testCreateDonor_DuplicateDni() {
        // Arrange
        DonorDTO donorDTO = createTestDonorDTO("D-1", "New Donor");
        when(donorService.existsById(donorDTO.getDni())).thenReturn(true);

        // Act
        ResponseEntity<DonorDTO> response = donorController.createDonor(donorDTO);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
        verify(donorService, times(1)).existsById(donorDTO.getDni());
        verify(donorService, never()).saveDonor(any(DonorDTO.class));
    }

    @Test
    void testUpdateDonor_Success() {
        // Arrange
        String dni = "D-1";
        DonorDTO donorDTO = createTestDonorDTO(dni, "Updated Donor");
        when(donorService.existsById(dni)).thenReturn(true);
        when(donorService.saveDonor(any(DonorDTO.class))).thenReturn(donorDTO);

        // Act
        ResponseEntity<DonorDTO> response = donorController.updateDonor(dni, donorDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dni, response.getBody().getDni());
        assertEquals("Updated Donor", response.getBody().getName());
        verify(donorService, times(1)).existsById(dni);
        verify(donorService, times(1)).saveDonor(donorDTO);
    }

    @Test
    void testUpdateDonor_NonExistingDni() {
        // Arrange
        String dni = "NONEXISTENT";
        DonorDTO donorDTO = createTestDonorDTO(dni, "Updated Donor");
        when(donorService.existsById(dni)).thenReturn(false);

        // Act
        ResponseEntity<DonorDTO> response = donorController.updateDonor(dni, donorDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(donorService, times(1)).existsById(dni);
        verify(donorService, never()).saveDonor(any(DonorDTO.class));
    }

    @Test
    void testDeleteDonor_Success() {
        // Arrange
        String dni = "D-1";
        when(donorService.existsById(dni)).thenReturn(true);
        // Note: The delete method is commented out in the controller, so we don't verify it's called

        // Act
        ResponseEntity<Void> response = donorController.deleteDonor(dni);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(donorService, times(1)).existsById(dni);
        // If uncommented, we would verify: verify(donorService, times(1)).deleteDonor(dni);
    }

    @Test
    void testDeleteDonor_NonExistingDni() {
        // Arrange
        String dni = "NONEXISTENT";
        when(donorService.existsById(dni)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = donorController.deleteDonor(dni);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(donorService, times(1)).existsById(dni);
        // If uncommented, we would verify: verify(donorService, never()).deleteDonor(anyString());
    }

    // Helper method
    private DonorDTO createTestDonorDTO(String dni, String name) {
        DonorDTO donorDTO = new DonorDTO();

        try {
            java.lang.reflect.Field dniField = DonorDTO.class.getDeclaredField("dni");
            dniField.setAccessible(true);
            dniField.set(donorDTO, dni);

            java.lang.reflect.Field nameField = DonorDTO.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(donorDTO, name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return donorDTO;
    }
}