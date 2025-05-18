package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.SurveyDTO;
import solidarityhub.backend.model.Survey;
import solidarityhub.backend.service.SurveyService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public ResponseEntity<?> getSurveys() {
        List<SurveyDTO> surveyDTOList = new ArrayList<>();
        surveyService.getAllSurveys().forEach(s -> {
            surveyDTOList.add(new SurveyDTO(s));
        });
        return ResponseEntity.ok(surveyDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSurvey(@PathVariable Integer id) {
        if (surveyService.getSurveyById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        Survey survey = surveyService.getSurveyById(id); //aquí se puede hacer refactoring
        return ResponseEntity.ok(new SurveyDTO(survey));

    }

    @PostMapping
    public ResponseEntity<?> addSurvey(@RequestBody SurveyDTO surveyDTO) {
        Survey survey = new Survey();
        survey.setSurveyName(surveyDTO.getName());
        survey.setSurveyCreationDate(surveyDTO.getCreatedDate());
        survey.setSurveyUpdatedAt(surveyDTO.getUpdatedAt());
        survey.setQuestions(surveyDTO.getQuestions());
        survey.setQualifications(surveyDTO.getQualifications());
        survey.setNumberOfAnswers(surveyDTO.getNumberOfAnswers());
        survey.setAverageQualification(surveyDTO.getAverageQualification());

        surveyService.save(survey);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSurvey(@PathVariable Integer id, @RequestBody SurveyDTO surveyDTO) {
        Survey survey = surveyService.getSurveyById(id);
        if (survey == null) {
            return ResponseEntity.notFound().build();
        }

        survey.setSurveyName(surveyDTO.getName());
        survey.setSurveyCreationDate(surveyDTO.getCreatedDate());
        survey.setSurveyUpdatedAt(surveyDTO.getUpdatedAt());
        survey.setQuestions(surveyDTO.getQuestions());
        survey.setQualifications(surveyDTO.getQualifications());
        survey.setNumberOfAnswers(surveyDTO.getNumberOfAnswers());
        survey.setAverageQualification(surveyDTO.getAverageQualification());

        surveyService.save(survey);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSurvey(@PathVariable Integer id) {
        if (surveyService.getSurveyById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        surveyService.deleteSurvey(surveyService.getSurveyById(id));

        return ResponseEntity.ok().build();
    }
}
