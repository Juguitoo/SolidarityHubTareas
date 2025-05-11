package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.dto.DonorDTO;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.repository.DonorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DonorServiceTest {

    @Mock
    private DonorRepository donorRepository;

    @InjectMocks
    private DonorService donorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllDonors() {
        // Arrange
        List<Donor> donors = new ArrayList<>();
        donors.add(createTestDonor("D-1", "Donor 1"));
        donors.add(createTestDonor("D-2", "Donor 2"));
        when(donorRepository.findAll()).thenReturn(donors);

        // Act
        List<DonorDTO> result = donorService.getAllDonors();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("D-1", result.get(0).getDni());
        assertEquals("Donor 1", result.get(0).getName());
        assertEquals("D-2", result.get(1).getDni());
        assertEquals("Donor 2", result.get(1).getName());
        verify(donorRepository, times(1)).findAll();
    }

    @Test
    void testGetDonorByDni_ExistingDni() {
        // Arrange
        String dni = "D-1";
        Donor donor = createTestDonor(dni, "Donor 1");
        when(donorRepository.findById(dni)).thenReturn(Optional.of(donor));

        // Act
        DonorDTO result = donorService.getDonorByDni(dni);

        // Assert
        assertNotNull(result);
        assertEquals(dni, result.getDni());
        assertEquals("Donor 1", result.getName());
        verify(donorRepository, times(1)).findById(dni);
    }

    @Test
    void testGetDonorByDni_NonExistingDni() {
        // Arrange
        String nonExistingDni = "NONEXISTENT";
        when(donorRepository.findById(nonExistingDni)).thenReturn(Optional.empty());

        // Act
        DonorDTO result = donorService.getDonorByDni(nonExistingDni);

        // Assert
        assertNull(result);
        verify(donorRepository, times(1)).findById(nonExistingDni);
    }

    @Test
    void testSaveDonor() {
        // Arrange
        DonorDTO donorDTO = createTestDonorDTO("D-1", "New Donor");
        Donor savedDonor = createTestDonor("D-1", "New Donor");
        when(donorRepository.save(any(Donor.class))).thenReturn(savedDonor);

        // Act
        DonorDTO result = donorService.saveDonor(donorDTO);

        // Assert
        assertNotNull(result);
        assertEquals("D-1", result.getDni());
        assertEquals("New Donor", result.getName());
        verify(donorRepository, times(1)).save(any(Donor.class));
    }

    @Test
    void testExistsById_Exists() {
        // Arrange
        String dni = "D-1";
        when(donorRepository.existsById(dni)).thenReturn(true);

        // Act
        boolean result = donorService.existsById(dni);

        // Assert
        assertTrue(result);
        verify(donorRepository, times(1)).existsById(dni);
    }

    @Test
    void testExistsById_NotExists() {
        // Arrange
        String dni = "NONEXISTENT";
        when(donorRepository.existsById(dni)).thenReturn(false);

        // Act
        boolean result = donorService.existsById(dni);

        // Assert
        assertFalse(result);
        verify(donorRepository, times(1)).existsById(dni);
    }

    @Test
    void testDeleteDonor() {
        // Arrange
        String dni = "D-1";
        doNothing().when(donorRepository).deleteById(anyString());

        // Act
        donorService.deleteDonor(dni);

        // Assert
        verify(donorRepository, times(1)).deleteById(dni);
    }

    // Helper methods
    private Donor createTestDonor(String dni, String name) {
        return new Donor(dni, name);
    }

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