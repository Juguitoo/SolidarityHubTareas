package solidarityhub.backend.controller;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import solidarityhub.backend.BackendApplication;
import solidarityhub.backend.config.TestConfig;
import solidarityhub.backend.dto.StorageDTO;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.repository.StorageRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class StorageControllerTest {

    @Autowired
    private StorageController storageController;
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void testGetStorages() {
        Storage storage1 = new Storage("storage 1" , new GPSCoordinates(0.0, 0.0), false, null);
        Storage storage2 = new Storage("storage 2" , new GPSCoordinates(0.0, 0.0), false, null);
        List<Storage> storages = List.of(storage1, storage2);

        List<Storage> storagesSaved = storageRepository.saveAll(storages);
        entityManager.flush();

        List<StorageDTO> storageDTOs = storagesSaved.stream()
                .map(StorageDTO::new)
                .toList();

        ResponseEntity<?> response = storageController.getStorages();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<StorageDTO> responseBody = (List<StorageDTO>) response.getBody();
        for( StorageDTO storageDTO : responseBody) {
            assertInstanceOf(StorageDTO.class, storageDTO);
            assertTrue(storageDTOs.contains(storageDTO));
        }

    }

    @Test
    void testGetStorage_ExistingId() {
        // Arrange
        Storage storage = new Storage("storage 1", new GPSCoordinates(0.0, 0.0), false, null);
        Storage savedStorage = storageRepository.save(storage);
        entityManager.flush();

        // Act
        ResponseEntity<?> response = storageController.getStorage(savedStorage.getId());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StorageDTO responseBody = (StorageDTO) response.getBody();
        assertNotNull(responseBody);
        assertEquals(new StorageDTO(savedStorage), responseBody);
    }

    @Test
    void testGetStorage_NonExistingId() {
        // Act
        ResponseEntity<?> response = storageController.getStorage(999);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateStorage_Success() {
        // Arrange
        Storage storage = new Storage("storage 1", new GPSCoordinates(0.0, 0.0), false, null);
        Storage savedStorage = storageRepository.save(storage);
        entityManager.flush();

        StorageDTO storageDTO = new StorageDTO(savedStorage.getId(), "Updated Storage", true);

        // Act
        ResponseEntity<?> response = storageController.updateStorage(savedStorage.getId(), storageDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        StorageDTO updatedStorage = storageRepository.findById(savedStorage.getId())
                .map(StorageDTO::new)
                .orElse(null);
        assertNotNull(updatedStorage);
        assertEquals(storageDTO.getName(), updatedStorage.getName());
    }

    @Test
    void testUpdateStorage_NonExistingId() {
        // Arrange
        StorageDTO storageDTO = new StorageDTO(999, "Updated Storage", true);

        // Act
        ResponseEntity<?> response = storageController.updateStorage(999, storageDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
