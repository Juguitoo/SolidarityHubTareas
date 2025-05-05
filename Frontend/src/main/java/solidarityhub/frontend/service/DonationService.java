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
import solidarityhub.frontend.model.Donation;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.model.enums.DonationStatus;

import java.time.LocalDate;
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
            clearCache(); // Añadir esta línea para limpiar la caché
        } catch (Exception e) {
            throw e;
        }
    }

    public void clearCache() {
        donationCache.clear();
    }

    // GET Methods
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
                return getExampleDonations();
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
            return getExampleDonations().stream()
                    .filter(d -> d.getCatastropheId() == catastropheId)
                    .toList();
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

    // Example data for testing or when backend is unavailable
    private List<DonationDTO> getExampleDonations() {
        List<DonationDTO> exampleDonations = new ArrayList<>();

        DonationDTO donation1 = new DonationDTO();
        donation1.setId(1);
        donation1.setCode("DON-2025-001");
        donation1.setType(DonationType.FINANCIAL);
        donation1.setDescription("Donación de $500");
        donation1.setDate(LocalDate.of(2025, 4, 5));
        donation1.setStatus(DonationStatus.COMPLETED);
        donation1.setQuantity(500);
        donation1.setUnit("€");
        donation1.setCantidad("500 €");
        donation1.setCatastropheId(1);
        donation1.setCatastropheName("Huracán Katrina");

        DonationDTO donation2 = new DonationDTO();
        donation2.setId(2);
        donation2.setCode("DON-2025-002");
        donation2.setType(DonationType.MATERIAL);
        donation2.setDescription("Ropa y medicinas");
        donation2.setDate(LocalDate.of(2025, 4, 6));
        donation2.setStatus(DonationStatus.IN_PROGRESS);
        donation2.setQuantity(50);
        donation2.setUnit("cajas");
        donation2.setCantidad("50 cajas");
        donation2.setCatastropheId(1);
        donation2.setCatastropheName("Huracán Katrina");

        DonationDTO donation3 = new DonationDTO();
        donation3.setId(3);
        donation3.setCode("DON-2025-003");
        donation3.setType(DonationType.SERVICE);
        donation3.setDescription("Transporte de suministros");
        donation3.setDate(LocalDate.of(2025, 4, 7));
        donation3.setStatus(DonationStatus.SCHEDULED);
        donation3.setQuantity(120);
        donation3.setUnit("horas");
        donation3.setCantidad("120 horas");
        donation3.setCatastropheId(1);
        donation3.setCatastropheName("Huracán Katrina");

        DonationDTO donation4 = new DonationDTO();
        donation4.setId(4);
        donation4.setCode("DON-2025-004");
        donation4.setType(DonationType.FINANCIAL);
        donation4.setDescription("Donación de $750");
        donation4.setDate(LocalDate.of(2025, 4, 8));
        donation4.setStatus(DonationStatus.COMPLETED);
        donation4.setQuantity(750);
        donation4.setUnit("€");
        donation4.setCantidad("750 €");
        donation4.setCatastropheId(1);
        donation4.setCatastropheName("Huracán Katrina");

        exampleDonations.add(donation1);
        exampleDonations.add(donation2);
        exampleDonations.add(donation3);
        exampleDonations.add(donation4);

        return exampleDonations;
    }
}
