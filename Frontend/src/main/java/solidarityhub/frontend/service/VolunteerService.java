package solidarityhub.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.model.Volunteer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class VolunteerService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public VolunteerService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/volunteers";
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
            objectMapper.registerModule(new JavaTimeModule());
            String taskDTOParams = objectMapper.writeValueAsString(taskDTO);
            String url = baseUrl + "?strategy=" + strategy + "&" + "taskString=" + URLEncoder.encode(taskDTOParams, StandardCharsets.UTF_8);
            ResponseEntity<VolunteerDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, null, VolunteerDTO[].class);
            VolunteerDTO[] volunteers = response.getBody();

            if (volunteers != null) {
                return List.of(volunteers);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public VolunteerDTO getVolunteerById(String id) {
        ResponseEntity<VolunteerDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, VolunteerDTO.class);
        return response.getBody();
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
}
