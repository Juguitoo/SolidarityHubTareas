package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.model.GPSCoordinates;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {
    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8082/solidarityhub/storages";
    private List<StorageDTO> storageCache;

    public StorageService() {
        this.restTemplate = new RestTemplate();
        this.storageCache = new ArrayList<>();
    }

    public List<StorageDTO> getStorages() {
        if (storageCache == null || storageCache.isEmpty()) {
            try {
                ResponseEntity<StorageDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, StorageDTO[].class);
                StorageDTO[] storages = response.getBody();
                if (storages != null) {
                    storageCache = new ArrayList<>(List.of(storages));
                } else {
                    storageCache = new ArrayList<>();
                }
            } catch (RestClientException e) {
                e.printStackTrace();
                return getExampleStorages(5);
            }
        }
        return storageCache;
    }

    public StorageDTO getStorageById(int id) {
        try {
            ResponseEntity<StorageDTO> response = restTemplate.getForEntity(baseUrl + "/" + id, StorageDTO.class);
            return response.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Almacenes de ejemplo
    private List<StorageDTO> getExampleStorages(int limit) {
        List<StorageDTO> exampleStorages = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            exampleStorages.add(new StorageDTO("Almacén Ejemplo " + (i + 1), new GPSCoordinates(0,0), false, new ArrayList<>()));
        }
        return exampleStorages;
    }
}
