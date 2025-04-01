package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.*;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@ToString
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    private String name;

    @Setter
    private String description;

    @Setter
    @Enumerated(EnumType.STRING)
    private EmergencyLevel emergencyLevel;

    @Setter
    @ManyToMany
    @JoinTable(name = "catastrophic_zones",
            joinColumns = @JoinColumn(name = "zone_id"),
            inverseJoinColumns = @JoinColumn(name = "catastrophe_id"))
    private List<Catastrophe> catastrophes;

    @Setter
    @OneToMany(mappedBy = "zone")
    private List<Storage> storages;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    @Setter
    @NonNull
    private List<GPSCoordinates> points;

    public Zone(String name, String description, EmergencyLevel emergencyLevel) {
        this.catastrophes = new ArrayList<>();
        this.storages = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.emergencyLevel = emergencyLevel;
        points = new ArrayList<>();
    }

    public void addStorage(Storage storage) {
        this.storages.add(storage);
    }
    public void addCatastrophe(Catastrophe catastrophe) {
        this.catastrophes.add(catastrophe);
    }
}