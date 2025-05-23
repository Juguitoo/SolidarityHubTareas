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
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.enums.ResourceType;
import solidarityhub.backend.repository.ResourceRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class ResourceControllerTest2 {

    @Autowired
    private ResourceController resourceController;

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void testGetResources() {
        Resource resource1 = new Resource("Resource 1", ResourceType.OTHER, 0, "unidades", null);
        Resource resource2 = new Resource("Resource 2", ResourceType.OTHER, 0, "unidades", null);
        List<Resource> resources = List.of(resource1, resource2);
        List<ResourceDTO> resourceDTOs = new ArrayList<>();
        resources.forEach(resource -> resourceDTOs.add(new ResourceDTO(resource)));
        resourceRepository.saveAll(resources);
        entityManager.flush();

        ResponseEntity<?> response = resourceController.getResources("", "", "", null);
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ResourceDTO> responseBody = (List<ResourceDTO>) response.getBody();
        assertEquals(resourceDTOs.size(), responseBody.size());
        for (int i = 0; i < resources.size(); i++) {
            ResourceDTO resource = responseBody.get(i);
            assertEquals(resources.get(i).getName(), resource.getName());
            assertEquals(resources.get(i).getType(), resource.getType());
            assertEquals(resources.get(i).getQuantity(), resource.getQuantity());
            assertEquals(resources.get(i).getUnit(), resource.getUnit());
        }
    }
}
