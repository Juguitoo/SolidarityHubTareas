package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.model.Volunteer;

import java.util.ArrayList;
import java.util.List;

public class VolunteerService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public VolunteerService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/volunteers";
    }

    public List<VolunteerDTO> getVolunteers() {
        try {
            ResponseEntity<VolunteerDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, VolunteerDTO[].class);
            VolunteerDTO[] volunteers = response.getBody();
            if (volunteers != null) {
                return List.of(volunteers);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleVolunteers();
        }
    }

    public void addVolunteer(VolunteerDTO volunteerDTO) {
        restTemplate.postForEntity(baseUrl, volunteerDTO, VolunteerDTO.class);
    }

    public void updateVolunteer(String id, VolunteerDTO volunteerDTO) {
        restTemplate.put(baseUrl + "/" + id, volunteerDTO);
    }

    public void deleteVolunteer(String id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public VolunteerDTO getVolunteer(String id) {
        ResponseEntity<VolunteerDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, VolunteerDTO.class);
        return response.getBody();
    }

    public List<VolunteerDTO> getExampleVolunteers() {
        List<VolunteerDTO> volunteerDTOs = new ArrayList<>();
        volunteerDTOs.add(new VolunteerDTO(new Volunteer("33", "Fernando", "Alonso", "alonso@astonmartin.com")));
        volunteerDTOs.add(new VolunteerDTO(new Volunteer("24", "Carlos", "Alvarez", "carlos@levante.com")));
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
}
