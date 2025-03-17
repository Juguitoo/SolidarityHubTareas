package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class GPSCoordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    public GPSCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
