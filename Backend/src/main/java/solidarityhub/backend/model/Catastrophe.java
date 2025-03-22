package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Catastrophe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int iD;

    @Setter
    @Column(nullable = false)
    @NonNull
    private String name;

    @Setter
    private String description;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    @Setter
    @NonNull
    private GPSCoordinates location;

    @Setter
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Setter
    @Column(nullable = false)
    @NonNull
    private EmergencyLevel emergencyLevel;

    @Setter
    @OneToMany(mappedBy = "catastrophe")
    private List<Need> needs;

    @Setter
    @ManyToMany(mappedBy = "catastrophes")
    private List<Zone> zones;

    public Catastrophe(String name, String description, GPSCoordinates location, LocalDate startDate , EmergencyLevel emergencyLevel) {
        this.zones = new ArrayList<>();
        this.needs = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.emergencyLevel = emergencyLevel;
    }
}
