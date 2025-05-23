package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.model.Need;
import org.pingu.domain.enums.TaskType;
import org.pingu.domain.enums.UrgencyLevel;

import java.util.ArrayList;
import java.util.List;

@Service
public class NeedService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NeedService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/needs";
    }

    //CRUD METHODS
    public void addNeed(NeedDTO needDTO) {
        restTemplate.postForEntity(baseUrl, needDTO, NeedDTO.class);
    }

    public void updateNeed(int id, NeedDTO needDTO) {
        restTemplate.put(baseUrl + "/" + id, needDTO);
    }

    public void deleteNeed(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    //GET METHODS
    public List<NeedDTO> getNeedsWithoutTask(Integer id) {
        try {
            ResponseEntity<NeedDTO[]> response = restTemplate.exchange(baseUrl + "/withoutTask?catastropheid=" + id, HttpMethod.GET, null, NeedDTO[].class);
            NeedDTO[] needs = response.getBody();
            if (needs != null) {
                return List.of(needs);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleNeeds();
        }
    }

    public List<NeedDTO> getAllNeeds(Integer id) {
        try {
            ResponseEntity<NeedDTO[]> response = restTemplate.exchange(baseUrl + "?catastropheid=" + id, HttpMethod.GET, null, NeedDTO[].class);
            NeedDTO[] needs = response.getBody();
            if (needs != null) {
                return List.of(needs);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleNeeds();
        }
    }

    //GET EXAMPLE NEEDS
    public List<NeedDTO> getExampleNeeds() {
        List<NeedDTO> needDTOs = new ArrayList<>();
        needDTOs.add(new NeedDTO(new Need("Material de construcción", UrgencyLevel.MODERATE, TaskType.LOGISTICS, null, null)));
        needDTOs.add(new NeedDTO(new Need("Alimentos no perecederos", UrgencyLevel.URGENT, TaskType.FEED, null, null)));
        return needDTOs;
    }

    //Metodo para convertir NeedDTO a Need
    public List<Need> convertToNeedList(List<NeedDTO> needDTOs) {
        List<Need> needs = new ArrayList<>();
        for (NeedDTO dto : needDTOs) {
            needs.add(new Need(
                    dto.getDescription(),
                    dto.getUrgency(),
                    dto.getTaskType(),
                    null,
                    null
            ));
        }
        return needs;
    }

    public int getNeedsWithoutTaskCount(Integer id) {
        try {
            ResponseEntity<Integer> response = restTemplate.exchange(baseUrl + "/countWithoutTask?catastropheid=" + id, HttpMethod.GET, null, Integer.class);
            return response.getBody();
        } catch (Exception e) {
            return 0;
        }
    }
}
