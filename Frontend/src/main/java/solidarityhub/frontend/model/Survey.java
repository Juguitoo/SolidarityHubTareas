package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@Getter
public class Survey {
    private int id;

    @Setter
    private String surveyName;

    @Setter
    private LocalDateTime surveyCreationDate;

    @Setter
    private LocalDateTime surveyUpdatedAt;

    @Setter
    private Map<Integer, String> questions;

    @Setter
    private Map<Integer, Double> qualifications;

    @Setter
    private int numberOfAnswers;

    @Setter
    private Double averageQualification;

    public Survey(String surveyName, LocalDateTime surveyCreationDate, LocalDateTime surveyUpdatedAt,
                  Map<Integer, String> questions, Map<Integer, Double> qualifications, int numberOfAnswers,
                  Double averageQualification) {
        this.surveyName = surveyName;
        this.surveyCreationDate = surveyCreationDate;
        this.surveyUpdatedAt = surveyUpdatedAt;
        this.questions = questions;
        this.qualifications = qualifications;
        this.numberOfAnswers = numberOfAnswers;
        this.averageQualification = averageQualification;
    }

}
