package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SurveyDTO {
    private int id;
    private String name;
    private LocalDateTime createdDate;
    private LocalDateTime updatedAt;
    private List<String> questions;
    private List<String> answers;
    private int numberOfAnswers;
    private Double averageQualification;

    public SurveyDTO(int id, String name, LocalDateTime createdDate, LocalDateTime updatedAt, List<String> questions, List<String> answers, int numberOfAnswers, Double averageQualification) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
        this.updatedAt = updatedAt;
        this.questions = questions;
        this.answers = answers;
        this.numberOfAnswers = numberOfAnswers;
        this.averageQualification = averageQualification;
    }

}
