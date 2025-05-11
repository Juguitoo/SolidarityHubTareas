package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.DonationStatus;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DonationDTOTest {

    @Test
    void testDonationDTOConstructor() {
        // Arrange
        Donation donation = createTestDonation();

        // Act
        DonationDTO dto = new DonationDTO(donation);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals(DonationType.MATERIAL, dto.getType());
        assertEquals("Test Donation", dto.getDescription());
        assertEquals(LocalDate.of(2025, 1, 1), dto.getDate());
        assertEquals(DonationStatus.COMPLETED, dto.getStatus());
        assertEquals("D-1", dto.getDonorDni());
        assertEquals("Test Donor", dto.getDonorName());
        assertEquals(Integer.valueOf(1), dto.getCatastropheId());
        assertEquals(10.0, dto.getQuantity());
        assertEquals("kg", dto.getUnit());
        assertEquals("10.0 kg", dto.getCantidad());
    }

    @Test
    void testDonationDTONoArgsConstructor() {
        // Act
        DonationDTO dto = new DonationDTO();

        // Assert
        assertNotNull(dto);
    }

    @Test
    void testGetCantidad_WithEmptyCantidad() {
        // Arrange
        DonationDTO dto = new DonationDTO();

        try {
            java.lang.reflect.Field quantityField = DonationDTO.class.getDeclaredField("quantity");
            quantityField.setAccessible(true);
            quantityField.set(dto, 5.0);

            java.lang.reflect.Field unitField = DonationDTO.class.getDeclaredField("unit");
            unitField.setAccessible(true);
            unitField.set(dto, "l");

            // Asegurarnos que cantidad es null o vacío
            java.lang.reflect.Field cantidadField = DonationDTO.class.getDeclaredField("cantidad");
            cantidadField.setAccessible(true);
            cantidadField.set(dto, null);
        } catch (Exception e) {
            fail("Failed to set fields: " + e.getMessage());
        }

        // Act
        String cantidad = dto.getCantidad();

        // Assert
        assertEquals("5.0 l", cantidad);
    }

    @Test
    void testGetCantidad_WithExistingCantidad() {
        // Arrange
        DonationDTO dto = new DonationDTO();

        try {
            java.lang.reflect.Field cantidadField = DonationDTO.class.getDeclaredField("cantidad");
            cantidadField.setAccessible(true);
            cantidadField.set(dto, "Cantidad predefinida");
        } catch (Exception e) {
            fail("Failed to set cantidad field: " + e.getMessage());
        }

        // Act
        String cantidad = dto.getCantidad();

        // Assert
        assertEquals("Cantidad predefinida", cantidad);
    }

    // Helper method
    private Donation createTestDonation() {
        Donor donor = new Donor("D-1", "Test Donor");

        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe",
                "Test Description",
                coordinates,
                LocalDate.now(),
                EmergencyLevel.MEDIUM
        );

        // Set ID in catastrophe
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, 1);
        } catch (Exception e) {
            fail("Failed to set catastrophe ID: " + e.getMessage());
        }

        Donation donation = new Donation(
                DonationType.MATERIAL,
                "Test Donation",
                LocalDate.of(2025, 1, 1),
                DonationStatus.COMPLETED,
                donor,
                catastrophe,
                10.0,
                "kg"
        );

        // Set ID in donation
        try {
            java.lang.reflect.Field idField = Donation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(donation, 1);
        } catch (Exception e) {
            fail("Failed to set donation ID: " + e.getMessage());
        }

        return donation;
    }
}