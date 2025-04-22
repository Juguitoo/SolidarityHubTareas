package solidarityhub.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CoordinatesService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String format;

    public CoordinatesService(){
        this.restTemplate = new RestTemplate();
        this.baseUrl = "https://nominatim.openstreetmap.org/search?q=";
        this.format = "&format=json&addressdetails=1";
    }

    public Map<String, Double> getCoordinates(String address) {
        String url = baseUrl + address.replace(" ", "+") + format;
        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object>[] responseMap = objectMapper.readValue(response, Map[].class);

            Map<String, Double> coordinatesMap = new HashMap<>();
            Double lat = responseMap[0].containsKey("lat") ? Double.parseDouble(responseMap[0].get("lat").toString()) : null;
            Double lon = responseMap[0].containsKey("lon") ? Double.parseDouble(responseMap[0].get("lon").toString()) : null;

            coordinatesMap.put("lat", lat);
            coordinatesMap.put("lon", lon);

            return coordinatesMap;
        } catch (Exception e) {
            return null;
        }
    }
}
