package solidarityhub.frontend.service;

import solidarityhub.frontend.dto.CatastropheDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CatastropheService {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8082/catastrophes";

    public CatastropheService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Obtiene todas las catástrofes del backend
     */
    public List<CatastropheDTO> getAllCatastrophes() {
        try {
            CatastropheDTO[] catastrophes = restTemplate.getForObject(baseUrl, CatastropheDTO[].class);
            return List.of(catastrophes);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene una catástrofe por su ID
     */
    public CatastropheDTO getCatastropheById(Integer id) {
        try {
            return restTemplate.getForObject(baseUrl + "/" + id, CatastropheDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Guarda una nueva catástrofe
     */
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

    /**
     * Actualiza una catástrofe existente
     */
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

    /**
     * Elimina una catástrofe por su ID
     */
    public void deleteCatastrophe(Integer id) {
        try {
            restTemplate.delete(baseUrl + "/" + id);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
