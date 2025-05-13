package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.ResourceType;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDTOTest {

    @Test
    void testResourceDTOConstructor_WithCatastrophe() {
        // Arrange
        Resource resource = createTestResource();
        Catastrophe catastrophe = createTestCatastrophe();
        resource.setCatastrophe(catastrophe);

        // Act
        ResourceDTO dto = new ResourceDTO(resource);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Resource", dto.getName());
        assertEquals(ResourceType.FOOD, dto.getType());
        assertEquals(10.0, dto.getQuantity());
        assertEquals("kg", dto.getUnit());
        assertEquals(1, dto.getStorageId());
        assertEquals(Integer.valueOf(1), dto.getCatastropheId());
    }

    @Test
    void testResourceDTOConstructor_WithoutCatastrophe() {
        // Arrange
        Resource resource = createTestResource();
        resource.setCatastrophe(null);

        // Act
        ResourceDTO dto = new ResourceDTO(resource);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Resource", dto.getName());
        assertEquals(ResourceType.FOOD, dto.getType());
        assertEquals(10.0, dto.getQuantity());
        assertEquals("kg", dto.getUnit());
        assertEquals(1, dto.getStorageId());
        assertNull(dto.getCatastropheId());
    }

    @Test
    void testResourceDTONoArgsConstructor() {
        // Act
        ResourceDTO dto = new ResourceDTO();

        // Assert
        assertNotNull(dto);
    }

    @Test
    void testGetCantidad() {
        // Arrange
        ResourceDTO dto = new ResourceDTO();

        try {
            java.lang.reflect.Field quantityField = ResourceDTO.class.getDeclaredField("quantity");
            quantityField.setAccessible(true);
            quantityField.set(dto, 5.5);

            java.lang.reflect.Field unitField = ResourceDTO.class.getDeclaredField("unit");
            unitField.setAccessible(true);
            unitField.set(dto, "l");
        } catch (Exception e) {
            fail("Failed to set fields: " + e.getMessage());
        }

        // Act
        String cantidad = dto.getCantidad();

        // Assert
        assertEquals("5.5 l", cantidad);
    }

    @Test
    void testResourceDTOSetters() {
        // Arrange
        ResourceDTO dto = new ResourceDTO();

        // Act
        dto.setId(2);
        dto.setName("Updated Resource");
        dto.setType(ResourceType.MEDICINE);
        dto.setQuantity(20.0);
        dto.setUnit("mg");
        dto.setStorageId(3);
        dto.setCatastropheId(4);

        // Assert
        assertEquals(2, dto.getId());
        assertEquals("Updated Resource", dto.getName());
        assertEquals(ResourceType.MEDICINE, dto.getType());
        assertEquals(20.0, dto.getQuantity());
        assertEquals("mg", dto.getUnit());
        assertEquals(3, dto.getStorageId());
        assertEquals(Integer.valueOf(4), dto.getCatastropheId());
        assertEquals("20.0 mg", dto.getCantidad());
    }

    // Helper methods
    private Resource createTestResource() {
        Storage storage = createTestStorage();

        Resource resource = new Resource(
                "Test Resource",
                ResourceType.FOOD,
                10.0,
                "kg",
                storage,
                null // Catastrophe se establece en los tests según sea necesario
        );

        // Set resource ID
        try {
            java.lang.reflect.Field idField = Resource.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(resource, 1);
        } catch (Exception e) {
            fail("Failed to set resource ID: " + e.getMessage());
        }

        return resource;
    }

    private Storage createTestStorage() {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Storage storage = new Storage(
                "Test Storage",
                coordinates,
                false,
                null
        );

        // Set storage ID
        try {
            java.lang.reflect.Field idField = Storage.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(storage, 1);
        } catch (Exception e) {
            fail("Failed to set storage ID: " + e.getMessage());
        }

        return storage;
    }

    private Catastrophe createTestCatastrophe() {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe",
                "Test Description",
                coordinates,
                LocalDate.now(),
                EmergencyLevel.MEDIUM
        );

        // Set catastrophe ID
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, 1);
        } catch (Exception e) {
            fail("Failed to set catastrophe ID: " + e.getMessage());
        }

        return catastrophe;
    }
}