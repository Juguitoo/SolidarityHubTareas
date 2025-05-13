package solidarityhub.frontend.service;

import org.springframework.web.client.RestTemplate;
import org.pingu.domain.DTO.DonorDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DonorService {
    private final String API_URL = "http://localhost:8082/solidarityhub/donors";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<DonorDTO> getAllDonors() {
        try {
            DonorDTO[] donors = restTemplate.getForObject(API_URL, DonorDTO[].class);
            return donors != null ? Arrays.asList(donors) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error al obtener los donantes: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public DonorDTO getDonorByDni(String dni) {
        try {
            return restTemplate.getForObject(API_URL + "/" + dni, DonorDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void addDonor(DonorDTO donor) {
        try {
            restTemplate.postForObject(API_URL, donor, DonorDTO.class);
        } catch (Exception e) {
            System.err.println("Error al agregar el donante: " + e.getMessage());
        }
    }
}
