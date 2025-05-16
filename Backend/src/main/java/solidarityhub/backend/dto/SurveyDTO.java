package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Survey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class SurveyDTO {
    private int id;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime updatedAt;
    private Map<Integer, String> questions;
    private Map<Integer, Double> qualifications;
    private int numberOfAnswers;
    private Double averageQualification;

    public SurveyDTO(int id, String name, LocalDateTime createdDate, LocalDateTime updatedAt, Map<Integer,
            String> questions, Map<Integer, Double> qualifications, int numberOfAnswers, Double averageQualification) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
        this.updatedAt = updatedAt;
        this.questions = questions;
        this.qualifications = qualifications;
        this.numberOfAnswers = numberOfAnswers;
        this.averageQualification = averageQualification;
    }

    public SurveyDTO(Survey survey) {
        this.id = survey.getId();
        this.name = survey.getSurveyName();
        this.createdDate = survey.getSurveyCreationDate();
        this.updatedAt = survey.getSurveyUpdatedAt();
        this.questions = survey.getQuestions();
        this.qualifications = survey.getQualifications();
        this.numberOfAnswers = survey.getNumberOfAnswers();
        this.averageQualification = survey.getAverageQualification();
    }

}
