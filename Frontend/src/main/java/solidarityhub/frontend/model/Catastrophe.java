package solidarityhub.frontend.model;

import org.pingu.domain.enums.EmergencyLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Catastrophe {
    private int id;

    @Setter
    private String name;

    @Setter
    private String description;
    
    @Setter
    private GPSCoordinates location;

    @Setter
    private LocalDate startDate;
    
    @Setter
    private EmergencyLevel emergencyLevel;

    @Setter
    private List<Need> needs;


    public Catastrophe(String name, String description, GPSCoordinates location, LocalDate startDate , EmergencyLevel emergencyLevel) {
        this.needs = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.emergencyLevel = emergencyLevel;
    }
}
