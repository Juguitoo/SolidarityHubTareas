package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GPSCoordinates gpsCoordinates;

    @Column(nullable = false)
    @Setter
    private boolean isFull;

    @Setter
    @OneToMany(mappedBy = "storage")
    private List<Resource> resources;

    @Setter
    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    public Storage(String name, GPSCoordinates gpsCoordinates, boolean isFull, Zone zone) {
        this.resources = new ArrayList<>();
        this.name = name;
        this.gpsCoordinates = gpsCoordinates;
        this.isFull = isFull;
        this.zone = zone;
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
    }
}
