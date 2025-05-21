package solidarityhub.frontend.service;

import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.SurveyDTO;

public class SurveyService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SurveyService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/surveys";
    }

    //CRUD METHODS
    public void addSurvey(SurveyDTO surveyDTO) {
        restTemplate.postForEntity(baseUrl, surveyDTO, SurveyDTO.class);
    }

    public void updateSurvey(int id, SurveyDTO surveyDTO) {
        restTemplate.put(baseUrl + "/" + id, surveyDTO);
    }

    public void deleteSurvey(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    //GET METHODS

    public SurveyDTO getSurveys() {
        return restTemplate.getForObject(baseUrl, SurveyDTO.class);
    }

    public SurveyDTO getSurveyById(int id) {
        return restTemplate.getForObject(baseUrl + "/" + id, SurveyDTO.class);
    }

}
