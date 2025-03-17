package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    private Set<Need> needs;

    @Setter
    @ManyToMany(mappedBy = "catastrophes")
    private Set<Zone> zones;

    public Catastrophe(String name, GPSCoordinates location, LocalDate startDate , EmergencyLevel emergencyLevel) {
        this.zones = new HashSet<>();
        this.name = name;
        this.location = location;
        this.startDate = startDate;
        this.emergencyLevel = emergencyLevel;
    }
}
