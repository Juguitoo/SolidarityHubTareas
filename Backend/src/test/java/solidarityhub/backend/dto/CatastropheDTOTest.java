package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CatastropheDTOTest {

    @Test
    void testCatastropheDTOConstructor() {
        // Arrange
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe",
                "Test Description",
                coordinates,
                LocalDate.of(2025, 1, 1),
                EmergencyLevel.MEDIUM
        );

        // Set ID using reflection
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, 1);
        } catch (Exception e) {
            fail("Failed to set ID field: " + e.getMessage());
        }

        // Act
        CatastropheDTO dto = new CatastropheDTO(catastrophe);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Catastrophe", dto.getName());
        assertEquals("Test Description", dto.getDescription());
        assertEquals(-3.703790, dto.getLocationX());
        assertEquals(40.416775, dto.getLocationY());
        assertEquals(LocalDate.of(2025, 1, 1), dto.getStartDate());
        assertEquals(EmergencyLevel.MEDIUM, dto.getEmergencyLevel());
    }

    @Test
    void testCatastropheDTONoArgsConstructor() {
        // Act
        CatastropheDTO dto = new CatastropheDTO();

        // Assert
        assertNotNull(dto);
        // No podemos verificar valores específicos ya que todos los campos son null
        // Solo verificamos que la instancia se crea correctamente
    }
}