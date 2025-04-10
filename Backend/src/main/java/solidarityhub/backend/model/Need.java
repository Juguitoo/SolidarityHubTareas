package solidarityhub.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.UrgencyLevel;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
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
    private TaskType taskType;

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

    @Setter
    private LocalDateTime startTimeDate;

    public Need(Affected affected, String description, UrgencyLevel urgency, TaskType needType, GPSCoordinates location, Catastrophe catastrophe) {
        this.affected = affected;
        this.description = description;
        this.urgency = urgency;
        this.taskType = needType;
        this.location = location;
        this.affected.addNeed(this);
        this.catastrophe = catastrophe;
        this.startTimeDate = LocalDateTime.now();
    }
}
