package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.repository.StorageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StorageServiceTest {

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStorages() {
        // Arrange
        List<Storage> storages = new ArrayList<>();
        storages.add(createTestStorage(1));
        storages.add(createTestStorage(2));
        when(storageRepository.findAll()).thenReturn(storages);

        // Act
        List<Storage> result = storageService.getStorages();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(storageRepository, times(1)).findAll();
    }

    @Test
    void testSaveStorage() {
        // Arrange
        Storage storage = createTestStorage(1);
        when(storageRepository.save(any(Storage.class))).thenReturn(storage);

        // Act
        Storage savedStorage = storageService.saveStorage(storage);

        // Assert
        assertNotNull(savedStorage);
        assertEquals(1, savedStorage.getId());
        assertEquals("Test Storage 1", savedStorage.getName());
        assertFalse(savedStorage.isFull());
        verify(storageRepository, times(1)).save(storage);
    }

    @Test
    void testGetStorageById_ExistingId() {
        // Arrange
        Integer id = 1;
        Storage storage = createTestStorage(id);
        when(storageRepository.findById(id)).thenReturn(Optional.of(storage));

        // Act
        Storage result = storageService.getStorageById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Storage 1", result.getName());
        verify(storageRepository, times(1)).findById(id);
    }

    @Test
    void testGetStorageById_NonExistingId() {
        // Arrange
        Integer nonExistingId = 999;
        when(storageRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Storage result = storageService.getStorageById(nonExistingId);

        // Assert
        assertNull(result);
        verify(storageRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void testDeleteStorage() {
        // Arrange
        Storage storage = createTestStorage(1);
        doNothing().when(storageRepository).delete(any(Storage.class));

        // Act
        storageService.deleteStorage(storage);

        // Assert
        verify(storageRepository, times(1)).delete(storage);
    }

    // Helper method
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
}