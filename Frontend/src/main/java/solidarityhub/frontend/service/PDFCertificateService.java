package solidarityhub.frontend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PDFCertificateService {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public PDFCertificateService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/certificates";
    }

    public void createPDFCertificate(int id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Integer> request = new HttpEntity<>(id, headers);
        restTemplate.postForObject(baseUrl, request, String.class);
    }
}
