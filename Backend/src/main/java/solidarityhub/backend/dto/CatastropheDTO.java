package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CatastropheDTO {
    private Integer id;
    private String name;
    private String description;
    private double locationX;
    private double locationY;
    private LocalDate startDate;
    private EmergencyLevel emergencyLevel;

    // Constructor completo
    public CatastropheDTO(Catastrophe catastrophe) {
        this.id = catastrophe.getId();
        this.name = catastrophe.getName();
        this.description = catastrophe.getDescription();
        this.locationX = catastrophe.getLocation().getLongitude();
        this.locationY = catastrophe.getLocation().getLatitude();
        this.startDate = catastrophe.getStartDate();
        this.emergencyLevel = catastrophe.getEmergencyLevel();
    }

}
