package solidarityhub.frontend.service;

import org.pingu.domain.enums.ResourceType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.ResourceSummaryDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ResourceSummaryService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ResourceSummaryService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/resources";
    }

    public List<ResourceSummaryDTO> getResourceSummary(int catastropheId) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl + "/summary?catastropheId=" + catastropheId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            List<ResourceSummaryDTO> summaries = new ArrayList<>();

            if (response.getBody() != null) {
                for (Map<String, Object> item : response.getBody()) {
                    ResourceType type = ResourceType.valueOf((String) item.get("type"));
                    int count = (Integer) item.get("count");
                    double totalQuantity = ((Number) item.get("totalQuantity")).doubleValue();
                    double assignedQuantity = ((Number) item.get("assignedQuantity")).doubleValue();

                    summaries.add(new ResourceSummaryDTO(type, count, totalQuantity, assignedQuantity));
                }
            }

            return summaries;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
