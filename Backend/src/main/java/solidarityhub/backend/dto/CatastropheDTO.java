package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.enums.EmergencyLevel;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatastropheDTO)) return false;

        CatastropheDTO that = (CatastropheDTO) o;

        if (Double.compare(that.locationX, locationX) != 0) return false;
        if (Double.compare(that.locationY, locationY) != 0) return false;
        if (!id.equals(that.id)) return false;
        if (!name.equals(that.name)) return false;
        if (!description.equals(that.description)) return false;
        if (!startDate.equals(that.startDate)) return false;
        return emergencyLevel == that.emergencyLevel;
    }
}
