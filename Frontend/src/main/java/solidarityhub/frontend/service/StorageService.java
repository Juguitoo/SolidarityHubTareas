package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.StorageDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                return new ArrayList<>();
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

    public List<String> getStorageNames() {
        return getStorages().stream()
                .map(StorageDTO::getName)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());
    }

    public void clearCache(){storageCache.clear();}
}
