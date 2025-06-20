package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.Catastrophe;
import org.pingu.domain.enums.EmergencyLevel;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CatastropheDTO {
    private Integer id;
    private String name;
    private String description;
    private double locationX;
    private double locationY;
    private LocalDate startDate;
    private EmergencyLevel emergencyLevel;

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
