package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Entity
public class Survey {
    @Id
    private int id;

    @Setter
    private String surveyName;

    @Setter
    private LocalDateTime surveyCreationDate;

    @Setter
    private LocalDateTime surveyUpdatedAt;

    @Setter
    @ElementCollection
    @CollectionTable(name = "survey_questions",
            joinColumns = @JoinColumn(name = "survey_id"))
    @MapKeyColumn(name = "id_question")
    @Column(name = "question")
    private Map<Integer, String> questions;

    @Setter
    @ElementCollection
    @CollectionTable(name = "survey_question_answers",
            joinColumns = @JoinColumn(name = "survey_id"))
    @MapKeyColumn(name = "question")
    @Column(name = "answer")
    private Map<Integer, Double> qualifications;

    @Setter
    private int numberOfAnswers;

    @Setter
    private Double averageQualification;

    public Survey(String surveyName, int numberOfAnswers, LocalDateTime surveyCreationDate, LocalDateTime surveyUpdatedAt,Map<Integer, String> questions, Map<Integer, Double> qualifications, Double averageQualification) {
        this.surveyName = surveyName;
        this.surveyCreationDate = surveyCreationDate;
        this.surveyUpdatedAt = surveyUpdatedAt;
        this.questions = questions;
        this.qualifications = qualifications;
        this.numberOfAnswers = numberOfAnswers;
        this.averageQualification = averageQualification;
    }

    public Survey() {
        this.surveyName = "Encuesta por defecto";
        this.surveyCreationDate = LocalDateTime.now();
        this.surveyUpdatedAt = LocalDateTime.now();
        this.questions = null;
        this.qualifications = null;
        this.numberOfAnswers = 0;
        this.averageQualification = 0.0;
    }
}
