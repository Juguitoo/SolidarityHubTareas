package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.model.Need;
import solidarityhub.frontend.model.enums.NeedType;
import solidarityhub.frontend.model.enums.UrgencyLevel;

import java.util.ArrayList;
import java.util.List;

public class NeedService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NeedService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/needs";
    }

    public List<NeedDTO> getNeeds() {
        try {
            ResponseEntity<NeedDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, NeedDTO[].class);
            NeedDTO[] needs = response.getBody();
            if (needs != null) {
                return List.of(needs);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleNeeds();
        }
    }

    public void addNeed(NeedDTO needDTO) {
        restTemplate.postForEntity(baseUrl, needDTO, NeedDTO.class);
    }

    public void updateNeed(int id, NeedDTO needDTO) {
        restTemplate.put(baseUrl + "/" + id, needDTO);
    }

    public void deleteNeed(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public NeedDTO getNeed(int id) {
        ResponseEntity<NeedDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, NeedDTO.class);
        return response.getBody();
    }

    public List<NeedDTO> getExampleNeeds() {
        List<NeedDTO> needDTOs = new ArrayList<>();
        needDTOs.add(new NeedDTO(new Need("Material de construcción", UrgencyLevel.MODERATE, NeedType.BUILDING, null, null)));
        needDTOs.add(new NeedDTO(new Need("Alimentos no perecederos", UrgencyLevel.URGENT, NeedType.FEED, null, null)));
        return needDTOs;
    }

    //Metodo para convertir NeedDTO a Need
    public List<Need> convertToNeedList(List<NeedDTO> needDTOs) {
        List<Need> needs = new ArrayList<>();
        for (NeedDTO dto : needDTOs) {
            needs.add(new Need(
                    dto.getDescription(),
                    dto.getUrgency(),
                    dto.getNeedType(),
                    null,
                    null
            ));
        }
        return needs;
    }
}
