package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
public class Storage {
    private int id;

    @Setter
    private String name;

    @Setter
    private GPSCoordinates gpsCoordinates;

    @Setter
    private boolean isFull;

    @Setter
    private List<Resource> resource;

    public Storage(String name, GPSCoordinates gpsCoordinates, boolean isFull) {
        this.name = name;
        this.gpsCoordinates = gpsCoordinates;
        this.isFull = isFull;
    }
}
