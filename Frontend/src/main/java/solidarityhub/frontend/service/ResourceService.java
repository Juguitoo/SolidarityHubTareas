package solidarityhub.frontend.service;

import org.pingu.domain.enums.ResourceType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.ResourceDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    public List<ResourceDTO> resourceCache;

    public ResourceService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/resources";
        this.resourceCache = new ArrayList<>();
    }

    //CRUD METHODS

    public void addResource(ResourceDTO resourceDTO) {
        restTemplate.postForEntity(baseUrl, resourceDTO, ResourceDTO.class);
    }

    //GET METHODS
    public void updateResource(int id, ResourceDTO resourceDTO) {
        restTemplate.put(baseUrl + "/" + id, resourceDTO);
    }

    public void deleteResource(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public void clearCache() {
        resourceCache.clear();
    }

    public List<ResourceDTO> getResources(String type, String minQuantity, String storageId, Integer catastropheId) {
        if (resourceCache == null || resourceCache.isEmpty()) {
            try {
                String url = baseUrl;
                if(type != null && !type.isEmpty()) {
                    url += "?type=" + type;
                }
                if(minQuantity != null && !minQuantity.isEmpty()) {
                    url += (url.contains("?") ? "&" : "?") + "minQuantity=" + minQuantity;
                }
                if(storageId != null && !storageId.isEmpty()) {
                    url += (url.contains("?") ? "&" : "?") + "storageId=" + storageId;
                }
                if(catastropheId != null) {
                    url += (url.contains("?") ? "&" : "?") + "catastropheId=" + catastropheId;
                }
                ResponseEntity<ResourceDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, null, ResourceDTO[].class);
                ResourceDTO[] resources = response.getBody();

                if (resources != null) {
                    resourceCache = new ArrayList<>(List.of(resources));
                } else {
                    resourceCache = new ArrayList<>();
                }

            } catch (RestClientException e) {
                throw new RuntimeException("Error fetching resources", e);
            }
        }
        return resourceCache;
    }

    public List<ResourceDTO> getResourcesByCatastropheId(int catastropheId) {
        try {
            String url = baseUrl + "?catastropheId=" +  catastropheId;
            ResponseEntity<ResourceDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, null, ResourceDTO[].class);
            ResourceDTO[] resources = response.getBody();
            if (resources != null) {
                return new ArrayList<>(List.of(resources));
            } else {
                return new ArrayList<>();
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error fetching resources by catastrophe ID", e);
        }
    }


    public List<ResourceDTO> getResourcesByType(Integer id, String type) {
        List<ResourceDTO> filteredResources = new ArrayList<>();
        for (ResourceDTO resource : resourceCache) {
            if (resource.getCatastropheId() == id && resource.getType().equals(type)) {
                filteredResources.add(resource);
            }
        }
        return filteredResources;
    }
}
