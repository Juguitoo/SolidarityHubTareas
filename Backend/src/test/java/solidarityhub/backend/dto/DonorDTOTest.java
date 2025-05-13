package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.Donor;

import static org.junit.jupiter.api.Assertions.*;

class DonorDTOTest {

    @Test
    void testDonorDTOConstructor() {
        // Arrange
        Donor donor = new Donor("D-1", "Test Donor");

        // Act
        DonorDTO dto = new DonorDTO(donor);

        // Assert
        assertEquals("D-1", dto.getDni());
        assertEquals("Test Donor", dto.getName());
    }

    @Test
    void testDonorDTONoArgsConstructor() {
        // Act
        DonorDTO dto = new DonorDTO();

        // Assert
        assertNotNull(dto);
    }

    @Test
    void testDonorDTOSetters() {
        // Arrange
        DonorDTO dto = new DonorDTO();

        // Act
        dto.setDni("D-2");
        dto.setName("Updated Donor");

        // Assert
        assertEquals("D-2", dto.getDni());
        assertEquals("Updated Donor", dto.getName());
    }
}