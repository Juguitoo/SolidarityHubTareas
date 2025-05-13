package solidarityhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        return ResponseEntity.ok(surveyDTOList);
    }
}
