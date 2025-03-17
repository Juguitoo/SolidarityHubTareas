package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.NeedType;
import solidarityhub.backend.model.enums.UrgencyLevel;

@Getter
@Entity
@NoArgsConstructor
public class Need {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "affected_dni")
    private Affected affected;

    @Setter
    private String description;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgency;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NeedType needType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "location_id")
    @Setter
    private GPSCoordinates location;

    @ManyToOne
    @JoinColumn(name = "catastrophe_id")
    @Setter
    private Catastrophe catastrophe;

    @Setter
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;


    public Need(Affected affected, String description, UrgencyLevel urgency, NeedType needType, GPSCoordinates location, Catastrophe catastrophe) {
        this.affected = affected;
        this.description = description;
        this.urgency = urgency;
        this.needType = needType;
        this.location = location;
        this.affected.addNeed(this);
        this.catastrophe = catastrophe;
    }
}
