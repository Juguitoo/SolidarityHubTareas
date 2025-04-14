package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.ResourceDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ResourceService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    public List<ResourceDTO> resourceChache;

    public ResourceService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/resources";
        this.resourceChache = new ArrayList<>();
    }

    //CRUD METHODS

    public void addResource(ResourceDTO resourceDTO) {
        restTemplate.postForEntity(baseUrl, resourceDTO, ResourceDTO.class);
    }

    public void updateResource(int id, ResourceDTO resourceDTO) {
        restTemplate.put(baseUrl + "/" + id, resourceDTO);
    }

    public void deleteResource(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public void clearCache() {
        resourceChache.clear();
    }

    //GET METHODS

    public List<ResourceDTO> getResources() {
        if (resourceChache == null || resourceChache.isEmpty()) {
            try {
                ResponseEntity<ResourceDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, ResourceDTO[].class);
                ResourceDTO[] resources = response.getBody();
                if (resources != null) {
                    resourceChache = new ArrayList<>(List.of(resources));
                } else {
                    resourceChache = new ArrayList<>();
                }
            } catch (RestClientException e) {
                return getExampleResources(5);
            }
        }
        return resourceChache;
    }

    public List<ResourceDTO> getResourcesByCatastropheId(int catastropheId) {
        return resourceChache.stream()
                .filter(resource -> resource.getCatastropheId() == catastropheId).toList();
    }

    //GET EXAMPLE RESOURCES

    private List<ResourceDTO> getExampleResources(int limit) {
        List<ResourceDTO> exampleResources = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            exampleResources.add(new ResourceDTO("Recurso ejemplo " + (i + 1), "Tipo " + (i + 1), i * 10));
        }
        return exampleResources;
    }
}
