package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.model.Survey;
import solidarityhub.backend.repository.SurveyRepository;

import java.util.List;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }
    public Survey save(Survey survey) {
        return this.surveyRepository.save(survey);
    }
    public List<Survey> getAllSurveys() {
        return this.surveyRepository.findAll();
    }
    public Survey getSurveyById(Integer id) {
        return this.surveyRepository.findById(id).orElse(null);
    }
    public void deleteSurvey(Survey survey) {
        this.surveyRepository.delete(survey);
    }
}
