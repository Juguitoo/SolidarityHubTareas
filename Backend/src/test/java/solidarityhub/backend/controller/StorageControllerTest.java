package solidarityhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.dto.StorageDTO;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.service.ResourceService;
import solidarityhub.backend.service.StorageService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StorageControllerTest {

    @Mock
    private StorageService storageService;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private StorageController storageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStorages() {
        // Arrange
        List<StorageDTO> storages = new ArrayList<>();
        storages.add(new StorageDTO(createTestStorage(1)));
        storages.add(new StorageDTO(createTestStorage(2)));
        when(storageService.getStorages()).thenReturn(storages);

        // Act
        ResponseEntity<?> response = storageController.getStorages();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof StorageDTO);
    }

    @Test
    void testGetStorage_ExistingId() {
        // Arrange
        int storageId = 1;
        Storage storage = createTestStorage(storageId);
        when(storageService.getStorageById(storageId)).thenReturn(storage);

        // Act
        ResponseEntity<?> response = storageController.getStorage(storageId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof StorageDTO);
        StorageDTO responseStorage = (StorageDTO) response.getBody();
        assertEquals(storageId, responseStorage.getId());
        assertEquals("Test Storage 1", responseStorage.getName());
    }

    @Test
    void testGetStorage_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(storageService.getStorageById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = storageController.getStorage(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateStorage_Success() {
        // Arrange
        int storageId = 1;
        StorageDTO storageDTO = createTestStorageDTO();
        Storage existingStorage = createTestStorage(storageId);
        when(storageService.getStorageById(storageId)).thenReturn(existingStorage);

        // Act
        ResponseEntity<?> response = storageController.updateStorage(storageId, storageDTO);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(storageService, times(1)).getStorageById(storageId);
        verify(storageService, times(1)).saveStorage(existingStorage);
    }

    @Test
    void testUpdateStorage_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        StorageDTO storageDTO = createTestStorageDTO();
        when(storageService.getStorageById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = storageController.updateStorage(nonExistingId, storageDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(storageService, times(1)).getStorageById(nonExistingId);
        verify(storageService, never()).saveStorage(any(Storage.class));
    }

    // Helper methods
    private Storage createTestStorage(int id) {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Storage storage = new Storage(
                "Test Storage " + id,
                coordinates,
                false,
                null
        );

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Storage.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(storage, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return storage;
    }

    private StorageDTO createTestStorageDTO() {
        StorageDTO dto = new StorageDTO();

        try {
            java.lang.reflect.Field idField = StorageDTO.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(dto, 1);

            java.lang.reflect.Field nameField = StorageDTO.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(dto, "Updated Storage");

            java.lang.reflect.Field isFullField = StorageDTO.class.getDeclaredField("isFull");
            isFullField.setAccessible(true);
            isFullField.set(dto, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }
}