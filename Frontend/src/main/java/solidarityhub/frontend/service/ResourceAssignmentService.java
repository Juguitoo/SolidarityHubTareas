package solidarityhub.frontend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.ResourceAssignmentDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ResourceAssignmentService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ResourceAssignmentService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/resource-assignments";
    }

    public void assignResourceToTask(int taskId, int resourceId, double quantity, String units) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResourceAssignmentDTO dto = new ResourceAssignmentDTO(taskId, resourceId, quantity, units);
            HttpEntity<ResourceAssignmentDTO> request = new HttpEntity<>(dto, headers);

            restTemplate.postForObject(baseUrl, request, ResourceAssignmentDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void assignResourceToTask(ResourceAssignmentDTO dto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ResourceAssignmentDTO> request = new HttpEntity<>(dto, headers);

            restTemplate.postForObject(baseUrl, request, ResourceAssignmentDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ResourceAssignmentDTO> getAssignmentsByTask(int taskId) {
        try {
            ResponseEntity<ResourceAssignmentDTO[]> response = restTemplate.exchange(
                    baseUrl + "/task/" + taskId,
                    HttpMethod.GET, null, ResourceAssignmentDTO[].class);

            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ResourceAssignmentDTO> getAssignmentsByResource(int resourceId) {
        try {
            ResponseEntity<ResourceAssignmentDTO[]> response = restTemplate.exchange(
                    baseUrl + "/resource/" + resourceId,
                    HttpMethod.GET, null, ResourceAssignmentDTO[].class);

            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Double getAvailableQuantity(int resourceId) {
        try {
            ResponseEntity<Double> response = restTemplate.exchange(
                    baseUrl + "/available/" + resourceId,
                    HttpMethod.GET, null, Double.class);

            return response.getBody();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public void deleteAssignment(int id) {
        try {
            restTemplate.delete(baseUrl + "/" + id);
        } catch (Exception e) {
            // Handle exception if needed
        }
    }

    public Double getTotalAssignedQuantity(int resourceId) {
        try {
            ResponseEntity<Double> response = restTemplate.exchange(
                    baseUrl + "/assigned-total/" + resourceId,
                    HttpMethod.GET, null, Double.class);

            return response.getBody();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
