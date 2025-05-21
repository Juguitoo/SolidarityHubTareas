package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

    public SurveyDTO(String name, LocalDateTime createdDate, LocalDateTime updatedAt, Map<Integer, String> questions,
                     Map<Integer, Double> qualifications, int numberOfAnswers, Double averageQualification) {
        this.name = name;
        this.createdDate = createdDate;
        this.updatedAt = updatedAt;
        this.questions = questions;
        this.qualifications = qualifications;
        this.numberOfAnswers = numberOfAnswers;
        this.averageQualification = averageQualification;
    }
}
