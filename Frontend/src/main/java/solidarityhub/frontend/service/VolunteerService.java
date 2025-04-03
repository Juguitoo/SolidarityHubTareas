package solidarityhub.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.model.Volunteer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VolunteerService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public VolunteerService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/volunteers";
    }

    //CRUD METHODS
    public void addVolunteer(VolunteerDTO volunteerDTO) {
        restTemplate.postForEntity(baseUrl, volunteerDTO, VolunteerDTO.class);
    }

    public void updateVolunteer(String id, VolunteerDTO volunteerDTO) {
        restTemplate.put(baseUrl + "/" + id, volunteerDTO);
    }

    public void deleteVolunteer(String id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    //GET METHODS
    public List<VolunteerDTO> getVolunteers(String strategy, TaskDTO taskDTO) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String taskDTOParams = objectMapper.writeValueAsString(taskDTO);
            System.out.println(taskDTOParams);
            String url = baseUrl + "?strategy=" + strategy + "&" + "taskString=" + URLEncoder.encode(taskDTOParams, StandardCharsets.UTF_8);
            ResponseEntity<VolunteerDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, null, VolunteerDTO[].class);
            VolunteerDTO[] volunteers = response.getBody();
            if (volunteers != null) {
                return List.of(volunteers);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return getExampleVolunteers();
        }
    }

    public VolunteerDTO getVolunteerById(String id) {
        ResponseEntity<VolunteerDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, VolunteerDTO.class);
        return response.getBody();
    }

    //GET EXAMPLE VOLUNTEERS
    public List<VolunteerDTO> getExampleVolunteers() {
        List<VolunteerDTO> volunteerDTOs = new ArrayList<>();
        volunteerDTOs.add(new VolunteerDTO(new Volunteer("33", "Fernando", "Alonso", "alonso@astonmartin.com")));
        volunteerDTOs.add(new VolunteerDTO(new Volunteer("24", "Carlos", "Alvarez", "carlos@levante.com")));
        volunteerDTOs.add(new VolunteerDTO(new Volunteer("27", "Sydney", "Sweeney", "Sweeney@gmail.com")));
        return volunteerDTOs;
    }

    //Metodo para convertir VolunteerDTO a Volunteer
    public List<Volunteer> convertToVolunteerList(List<VolunteerDTO> volunteerDTOs) {
        List<Volunteer> volunteers = new ArrayList<>();
        for (VolunteerDTO dto : volunteerDTOs) {
            volunteers.add(new Volunteer(
                    dto.getDni(),
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getEmail()
            ));
        }
        return volunteers;
    }
/*
    private String convertToQueryTaskDTO(TaskDTO taskDTO) {
        Map<String, String> params = Map.of(
                "id", taskDTO.getId() + "",
                "type", taskDTO.getType().toString(),
                "startTimeDate", taskDTO.getStartTimeDate().toString(),
                "estimatedEndTimeDate", taskDTO.getEstimatedEndTimeDate().toString());
        for (NeedDTO need : taskDTO.getNeeds()) {
            params.put("needs", need.toString());
        }

        return params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String convertToQueryNeedDTO(List<NeedDTO> needDTOs){
        Map<String, String> params = new HashMap<>();
        for (NeedDTO needDTO : needDTOs) {
            params.put( needDTO.getId() + "",
                    "latitude", needDTO.getLocation().getLatitude() + "",
                    "longitude", needDTO.getLocation().getLongitude() + ""));
        }

        return params.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

 */
}
