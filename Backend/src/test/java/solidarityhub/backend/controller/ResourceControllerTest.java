package solidarityhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.dto.ResourceDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.ResourceType;
import solidarityhub.backend.service.CatastropheService;
import solidarityhub.backend.service.ResourceService;
import solidarityhub.backend.service.StorageService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @Mock
    private CatastropheService catastropheService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ResourceController resourceController;

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
        when(resourceService.getResources()).thenReturn(resources);

        // Act
        ResponseEntity<?> response = resourceController.getResources(null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof ResourceDTO);
    }

    @Test
    void testGetResourcesByCatastrophe() {
        // Arrange
        int catastropheId = 1;
        List<Resource> resources = new ArrayList<>();
        resources.add(createTestResource(1));
        resources.add(createTestResource(2));
        when(resourceService.getResourcesByCatastrophe(catastropheId)).thenReturn(resources);

        // Act
        ResponseEntity<?> response = resourceController.getResources(null, null, null, catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof ResourceDTO);
    }

    @Test
    void testGetResource_ExistingId() {
        // Arrange
        int resourceId = 1;
        Resource resource = createTestResource(resourceId);
        when(resourceService.getResourceById(resourceId)).thenReturn(resource);

        // Act
        ResponseEntity<?> response = resourceController.getResource(resourceId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResourceDTO);
        ResourceDTO responseResource = (ResourceDTO) response.getBody();
        assertEquals(resourceId, responseResource.getId());
    }

    @Test
    void testGetResource_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(resourceService.getResourceById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = resourceController.getResource(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreateResource_Success() {
        // Arrange
        ResourceDTO resourceDTO = createTestResourceDTO();
        Catastrophe catastrophe = createTestCatastrophe(1);
        Storage storage = createTestStorage(1);

        when(catastropheService.getCatastrophe(resourceDTO.getCatastropheId())).thenReturn(catastrophe);
        when(storageService.getStorageById(resourceDTO.getStorageId())).thenReturn(storage);
        doAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            try {
                java.lang.reflect.Field idField = Resource.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(resource, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resource;
        }).when(resourceService).save(any(Resource.class));

        // Act
        ResponseEntity<?> response = resourceController.createResource(resourceDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(catastropheService, times(1)).getCatastrophe(resourceDTO.getCatastropheId());
        verify(storageService, times(1)).getStorageById(resourceDTO.getStorageId());
        verify(resourceService, times(1)).save(any(Resource.class));
    }

    @Test
    void testCreateResource_NoCatastropheId() {
        // Arrange
        ResourceDTO resourceDTO = createTestResourceDTO();
        resourceDTO.setCatastropheId(null);

        // Act
        ResponseEntity<?> response = resourceController.createResource(resourceDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El ID de la catástrofe es obligatorio", response.getBody());
        verify(catastropheService, never()).getCatastrophe(anyInt());
        verify(resourceService, never()).save(any(Resource.class));
    }

    @Test
    void testCreateResource_NonExistingCatastrophe() {
        // Arrange
        ResourceDTO resourceDTO = createTestResourceDTO();
        when(catastropheService.getCatastrophe(resourceDTO.getCatastropheId())).thenReturn(null);

        // Act
        ResponseEntity<?> response = resourceController.createResource(resourceDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(catastropheService, times(1)).getCatastrophe(resourceDTO.getCatastropheId());
        verify(resourceService, never()).save(any(Resource.class));
    }

    @Test
    void testUpdateResource_Success() {
        // Arrange
        int resourceId = 1;
        ResourceDTO resourceDTO = createTestResourceDTO();
        Resource existingResource = createTestResource(resourceId);
        Storage storage = createTestStorage(resourceDTO.getStorageId());

        when(resourceService.getResourceById(resourceId)).thenReturn(existingResource);
        when(storageService.getStorageById(resourceDTO.getStorageId())).thenReturn(storage);

        // Act
        ResponseEntity<?> response = resourceController.updateResource(resourceId, resourceDTO);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(resourceService, times(1)).getResourceById(resourceId);
        verify(storageService, times(1)).getStorageById(resourceDTO.getStorageId());
        verify(resourceService, times(1)).save(existingResource);
    }

    @Test
    void testUpdateResource_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        ResourceDTO resourceDTO = createTestResourceDTO();
        when(resourceService.getResourceById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = resourceController.updateResource(nonExistingId, resourceDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(resourceService, times(1)).getResourceById(nonExistingId);
        verify(resourceService, never()).save(any(Resource.class));
    }

    @Test
    void testDeleteResource_Success() {
        // Arrange
        int resourceId = 1;
        Resource existingResource = createTestResource(resourceId);
        when(resourceService.getResourceById(resourceId)).thenReturn(existingResource);
        doNothing().when(resourceService).deleteResource(existingResource);

        // Act
        ResponseEntity<?> response = resourceController.deleteResource(resourceId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(resourceService, times(1)).getResourceById(resourceId);
        verify(resourceService, times(1)).deleteResource(existingResource);
    }

    @Test
    void testDeleteResource_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(resourceService.getResourceById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = resourceController.deleteResource(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(resourceService, times(1)).getResourceById(nonExistingId);
        verify(resourceService, never()).deleteResource(any(Resource.class));
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

    private ResourceDTO createTestResourceDTO() {
        ResourceDTO dto = new ResourceDTO();

        try {
            java.lang.reflect.Field idField = ResourceDTO.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(dto, 1);

            java.lang.reflect.Field nameField = ResourceDTO.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(dto, "Test Resource");

            java.lang.reflect.Field typeField = ResourceDTO.class.getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(dto, ResourceType.FOOD);

            java.lang.reflect.Field quantityField = ResourceDTO.class.getDeclaredField("quantity");
            quantityField.setAccessible(true);
            quantityField.set(dto, 10.0);

            java.lang.reflect.Field unitField = ResourceDTO.class.getDeclaredField("unit");
            unitField.setAccessible(true);
            unitField.set(dto, "kg");

            java.lang.reflect.Field storageIdField = ResourceDTO.class.getDeclaredField("storageId");
            storageIdField.setAccessible(true);
            storageIdField.set(dto, 1);

            java.lang.reflect.Field catastropheIdField = ResourceDTO.class.getDeclaredField("catastropheId");
            catastropheIdField.setAccessible(true);
            catastropheIdField.set(dto, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }
}