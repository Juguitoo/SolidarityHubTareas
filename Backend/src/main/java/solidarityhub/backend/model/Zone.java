package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.*;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<Catastrophe> catastrophes;

    @Setter
    @OneToMany(mappedBy = "zone")
    private Set<Storage> storages;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    @Setter
    @NonNull
    private List<GPSCoordinates> points;

    public Zone(String name, String description, EmergencyLevel emergencyLevel) {
        this.catastrophes = new HashSet<>();
        this.storages = new HashSet<>();
        this.name = name;
        this.description = description;
        this.emergencyLevel = emergencyLevel;
        points = new ArrayList<>();
    }
}