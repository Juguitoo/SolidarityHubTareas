package solidarityhub.frontend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.model.Catastrophe;
import solidarityhub.frontend.model.GPSCoordinates;
import solidarityhub.frontend.model.enums.EmergencyLevel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CatastropheService {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8082/solidarityhub/catastrophes";

    public CatastropheService() {
        this.restTemplate = new RestTemplate();
    }

    //CRUD METHODS
    public CatastropheDTO saveCatastrophe(CatastropheDTO catastropheDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CatastropheDTO> request = new HttpEntity<>(catastropheDTO, headers);

            return restTemplate.postForObject(baseUrl, request, CatastropheDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Re-lanzar la excepción para manejarla en la vista
        }
    }

    public CatastropheDTO updateCatastrophe(Integer id, CatastropheDTO catastropheDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CatastropheDTO> request = new HttpEntity<>(catastropheDTO, headers);

            return restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.PUT,
                    request,
                    CatastropheDTO.class
            ).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteCatastrophe(Integer id) {
        try {
            restTemplate.delete(baseUrl + "/" + id);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //GET METHODS
    public List<CatastropheDTO> getAllCatastrophes() {
        try {
            CatastropheDTO[] catastrophes = restTemplate.getForObject(baseUrl, CatastropheDTO[].class);
            return catastrophes != null ? Arrays.asList(catastrophes) : Collections.emptyList();
        } catch (Exception e) {
            return getExampleCatastrophes();
        }
    }

    public CatastropheDTO getCatastropheById(Integer id) {
        try {
            return restTemplate.getForObject(baseUrl + "/" + id, CatastropheDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //GET EXAMPLE CATASROPHE
    public List<CatastropheDTO> getExampleCatastrophes() {
        List<CatastropheDTO> exampleCatastrophes = new ArrayList<>();

        Catastrophe expampleCatastrophe = new Catastrophe(
                "Catástrofe de ejemplo",
                "Descripción de la catástrofe de ejemplo",
                new GPSCoordinates(0.0, 0.0),
                LocalDate.now(),
                EmergencyLevel.HIGH
        );

        exampleCatastrophes.add(new CatastropheDTO(expampleCatastrophe));

        return exampleCatastrophes;
    }
}
