package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.GPSCoordinates;
import solidarityhub.frontend.model.Resource;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class StorageDTO {
    private int id;
    private String name;
    private GPSCoordinates gpsCoordinates;
    private boolean isFull;
    private List<Integer> resources;

    public StorageDTO(String name, GPSCoordinates gpsCoordinates, boolean isFull, List<Integer> resources) {
        this.name = name;
        this.gpsCoordinates = gpsCoordinates;
        this.isFull = isFull;
        this.resources = resources;
    }
}
