package solidarityhub.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import solidarityhub.backend.BackendApplication;
import solidarityhub.backend.config.TestConfig;
import solidarityhub.backend.dto.ResourceDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.ResourceType;
import solidarityhub.backend.repository.CatastropheRepository;
import solidarityhub.backend.repository.ResourceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class ResourceControllerTest {

    @Autowired
    private ResourceController resourceController;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private CatastropheRepository catastropheRepository;

    @Test
    void testGetResources() {
        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", null);
        Resource resource2 = new Resource("Resource 2", ResourceType.OTHER, 0, "unidades", null);
        List<Resource> resources = List.of(resource1, resource2);
        List<Resource> resourcesSaved = resourceRepository.saveAll(resources);
        entityManager.flush();

        List<ResourceDTO> resourceDTOs = new ArrayList<>();
        resourcesSaved.forEach(resource -> resourceDTOs.add(new ResourceDTO(resource)));
        entityManager.flush();

        ResponseEntity<?> response = resourceController.getResources("", "", "", null);
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ResourceDTO> responseBody = (List<ResourceDTO>) response.getBody();
        assertEquals(resourceDTOs.size(), responseBody.size());
        for (int i = 0; i < resourceDTOs.size(); i++) {
            assertEquals(resourceDTOs.get(i), responseBody.get(i));
        }
    }

    @Test
    void testGetResourcesByCatastrophe() {
        Catastrophe catastrophe = new Catastrophe("", "", null, LocalDate.now(), EmergencyLevel.MEDIUM);
        catastropheRepository.save(catastrophe);
        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", catastrophe);
        Resource resource2 = new Resource("Resource 2", ResourceType.OTHER, 0, "unidades", catastrophe);
        Resource resource3 = new Resource("Resource 3", ResourceType.OTHER, 0, "unidades", null);
        List<Resource> resources = List.of(resource1, resource2, resource3);
        List<Resource> resourcesSaved = resourceRepository.saveAll(resources);
        entityManager.flush();

        List<ResourceDTO> resourceDTOs = new ArrayList<>();
        resourcesSaved.stream().filter(r -> r.getCatastrophe()!= null).forEach(resource -> resourceDTOs.add(new ResourceDTO(resource)));
        entityManager.flush();

        ResponseEntity<?> response = resourceController.getResources(null, null, null, catastrophe.getId());
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ResourceDTO> responseBody = (List<ResourceDTO>) response.getBody();
        assertEquals(resourceDTOs.size(), responseBody.size());
        for (int i = 0; i < resourceDTOs.size(); i++) {
            assertTrue(resourceDTOs.get(i).equals(responseBody.get(i)));
        }
        assertFalse(responseBody.contains(new ResourceDTO(resource3)));
    }

    @Test
    void testGetResource_ExistingId() {
        Catastrophe catastrophe = new Catastrophe("", "", null, LocalDate.now(), EmergencyLevel.MEDIUM);
        catastropheRepository.save(catastrophe);
        Resource resource = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", catastrophe);
        resourceRepository.save(resource);
        entityManager.flush();

        ResponseEntity<?> response = resourceController.getResource(resource.getId());
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResourceDTO responseBody = (ResourceDTO) response.getBody();
        assertEquals(new ResourceDTO(resource), responseBody);
    }

    @Test
    void testGetResource_NonExistingId() {
        ResponseEntity<?> response = resourceController.getResource(999);
        assertNotNull(response);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = (String) response.getBody();
        assertEquals(null, responseBody);
    }

    @Test
    void testCreateResource_WithoutCatastrophe() {
        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", null);
        ResourceDTO resourceDTO = new ResourceDTO(resource1);

        ResponseEntity<?> response = resourceController.createResource(resourceDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = (String) response.getBody();
        assertEquals("El ID de la catástrofe es obligatorio", responseBody);
    }

    @Test
    void testCreateResource_Success() {
        Catastrophe catastrophe = new Catastrophe("", "", null, LocalDate.now(), EmergencyLevel.MEDIUM);
        catastropheRepository.save(catastrophe);
        Storage storage = new Storage("Storage 1", null, true,null);
        entityManager.persist(storage);
        entityManager.flush();

        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", catastrophe);
        ResourceDTO resourceDTO = new ResourceDTO(resource1);
        resourceDTO.setStorageId(storage.getId());
        ResponseEntity<?> response = resourceController.createResource(resourceDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Resource savedResource= resourceRepository.findAll().getFirst();
        assertNotNull(savedResource);
        assertEquals(resource1.getName(), savedResource.getName());
    }

    @Test
    void testCreateResource_NonExistingCatastrophe() {
        Catastrophe catastrophe = new Catastrophe("", "", null, LocalDate.now(), EmergencyLevel.MEDIUM);
        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", catastrophe);
        ResourceDTO resourceDTO = new ResourceDTO(resource1);

        ResponseEntity<?> response = resourceController.createResource(resourceDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        int numberOfResources = resourceRepository.findAll().size();
        assertEquals(0, numberOfResources);
    }

    @Test
    void testUpdateResource_Success() {
        Catastrophe catastrophe = new Catastrophe("", "", null, LocalDate.now(), EmergencyLevel.MEDIUM);
        catastropheRepository.save(catastrophe);
        Storage storage = new Storage("Storage 1", null, true,null);
        entityManager.persist(storage);
        entityManager.flush();

        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", catastrophe);
        resourceRepository.save(resource1);
        entityManager.flush();

        resource1.setName("Updated Resource");
        ResourceDTO resourceDTO = new ResourceDTO(resource1);
        resourceDTO.setStorageId(storage.getId());
        ResponseEntity<?> response = resourceController.updateResource(resource1.getId(), resourceDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Resource savedResource= resourceRepository.findAll().getFirst();
        assertNotNull(savedResource);
        assertEquals("Updated Resource", savedResource.getName());
    }

    @Test
    void testUpdateResource_NonExistingId() {
        ResourceDTO resourceDTO = new ResourceDTO();
        ResponseEntity<?> response = resourceController.updateResource(999, resourceDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteResource_Success() {
        Catastrophe catastrophe = new Catastrophe("", "", null, LocalDate.now(), EmergencyLevel.MEDIUM);
        catastropheRepository.save(catastrophe);
        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", catastrophe);
        Resource savedResource = resourceRepository.save(resource1);
        entityManager.flush();

        ResponseEntity<?> response = resourceController.deleteResource(savedResource.getId());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Resource deletedResource = resourceRepository.findById(savedResource.getId()).orElse(null);
        assertNull(deletedResource);
    }

    @Test
    void testDeleteResource_NonExistingId() {
        ResponseEntity<?> response = resourceController.deleteResource(999);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
