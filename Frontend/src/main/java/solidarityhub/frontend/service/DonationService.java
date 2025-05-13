package solidarityhub.frontend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.DonationDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DonationService {
    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8082/solidarityhub/donations";
    private List<DonationDTO> donationCache;

    public DonationService() {
        this.restTemplate = new RestTemplate();
        this.donationCache = new ArrayList<>();
    }

    // CRUD Methods
    public void addDonation(DonationDTO donationDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<DonationDTO> request = new HttpEntity<>(donationDTO, headers);
            restTemplate.postForEntity(baseUrl, request, DonationDTO.class);
            clearCache();
        } catch (Exception e) {
            throw e;
        }
    }

    public void updateDonation(int id, DonationDTO donationDTO) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<DonationDTO> request = new HttpEntity<>(donationDTO, headers);
            restTemplate.exchange(baseUrl + "/" + id, HttpMethod.PUT, request, DonationDTO.class);
            clearCache();
        } catch (Exception e) {
            throw e;
        }
    }

    public void deleteDonation(int id) {
        try {
            restTemplate.delete(baseUrl + "/" + id);
            clearCache();
        } catch (Exception e) {
            System.err.println("Error al eliminar la donación con ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    public void clearCache() {
        donationCache.clear();
    }

    public List<DonationDTO> getDonations() {
        if (donationCache == null || donationCache.isEmpty()) {
            try {
                ResponseEntity<DonationDTO[]> response = restTemplate.exchange(
                        baseUrl, HttpMethod.GET, null, DonationDTO[].class);
                DonationDTO[] donations = response.getBody();
                if (donations != null) {
                    donationCache = new ArrayList<>(Arrays.asList(donations));
                } else {
                    donationCache = new ArrayList<>();
                }
            } catch (RestClientException e) {
                return new ArrayList<>();
            }
        }
        return donationCache;
    }

    public List<DonationDTO> getDonationsByCatastrophe(int catastropheId) {
        try {
            ResponseEntity<DonationDTO[]> response = restTemplate.exchange(
                    baseUrl + "/byCatastrophe?catastropheId=" + catastropheId,
                    HttpMethod.GET, null, DonationDTO[].class);
            DonationDTO[] donations = response.getBody();
            if (donations != null) {
                return Arrays.asList(donations);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return new ArrayList<>();
        }
    }

    public DonationDTO getDonationById(int id) {
        try {
            ResponseEntity<DonationDTO> response = restTemplate.exchange(
                    baseUrl + "/" + id, HttpMethod.GET, null, DonationDTO.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
