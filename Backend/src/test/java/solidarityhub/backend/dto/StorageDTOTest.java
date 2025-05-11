package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Storage;

import static org.junit.jupiter.api.Assertions.*;

class StorageDTOTest {

    @Test
    void testStorageDTOConstructor() {
        // Arrange
        Storage storage = createTestStorage();

        // Act
        StorageDTO dto = new StorageDTO(storage);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Storage", dto.getName());
        assertFalse(dto.isFull());
    }

    @Test
    void testStorageDTONoArgsConstructor() {
        // Act
        StorageDTO dto = new StorageDTO();

        // Assert
        assertNotNull(dto);
    }

    @Test
    void testStorageDTOSetters() {
        // Arrange
        StorageDTO dto = new StorageDTO();

        // Act
        dto.setId(2);
        dto.setName("Updated Storage");
        dto.setFull(true);

        // Assert
        assertEquals(2, dto.getId());
        assertEquals("Updated Storage", dto.getName());
        assertTrue(dto.isFull());
    }

    // Helper method
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
}