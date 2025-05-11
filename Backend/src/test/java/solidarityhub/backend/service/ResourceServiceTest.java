package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.ResourceType;
import solidarityhub.backend.repository.ResourceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetResources() {
        // Arrange
        List<Resource> resources = new ArrayList<>();
        resources.add(createTestResource(1));
        resources.add(createTestResource(2));
        when(resourceRepository.findAll()).thenReturn(resources);

        // Act
        List<Resource> result = resourceService.getResources();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(resourceRepository, times(1)).findAll();
    }

    @Test
    void testSave() {
        // Arrange
        Resource resource = createTestResource(1);
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        // Act
        Resource savedResource = resourceService.save(resource);

        // Assert
        assertNotNull(savedResource);
        assertEquals(1, savedResource.getId());
        assertEquals("Test Resource 1", savedResource.getName());
        assertEquals(ResourceType.FOOD, savedResource.getType());
        verify(resourceRepository, times(1)).save(resource);
    }

    @Test
    void testGetResourceById_ExistingId() {
        // Arrange
        Integer id = 1;
        Resource resource = createTestResource(id);
        when(resourceRepository.findById(id)).thenReturn(Optional.of(resource));

        // Act
        Resource result = resourceService.getResourceById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Resource 1", result.getName());
        verify(resourceRepository, times(1)).findById(id);
    }

    @Test
    void testGetResourceById_NonExistingId() {
        // Arrange
        Integer nonExistingId = 999;
        when(resourceRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Resource result = resourceService.getResourceById(nonExistingId);

        // Assert
        assertNull(result);
        verify(resourceRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void testDeleteResource() {
        // Arrange
        Resource resource = createTestResource(1);
        doNothing().when(resourceRepository).delete(any(Resource.class));

        // Act
        resourceService.deleteResource(resource);

        // Assert
        verify(resourceRepository, times(1)).delete(resource);
    }

    @Test
    void testGetResourcesByCatastrophe() {
        // Arrange
        int catastropheId = 1;
        List<Resource> resources = new ArrayList<>();
        resources.add(createTestResource(1));
        resources.add(createTestResource(2));
        when(resourceRepository.getResourcesByCatastrophe(catastropheId)).thenReturn(resources);

        // Act
        List<Resource> result = resourceService.getResourcesByCatastrophe(catastropheId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(resourceRepository, times(1)).getResourcesByCatastrophe(catastropheId);
    }

    // Helper methods
    private Resource createTestResource(int id) {
        Catastrophe catastrophe = createTestCatastrophe(1);
        Storage storage = createTestStorage(1);

        Resource resource = new Resource(
                "Test Resource " + id,
                ResourceType.FOOD,
                10.0,
                "kg",
                storage,
                catastrophe
        );

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Resource.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(resource, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resource;
    }

    private Catastrophe createTestCatastrophe(int id) {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe " + id,
                "Test Description " + id,
                coordinates,
                LocalDate.now(),
                EmergencyLevel.MEDIUM
        );

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return catastrophe;
    }

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