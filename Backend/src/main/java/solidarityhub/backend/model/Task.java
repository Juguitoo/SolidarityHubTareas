package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER)
    private List<Need> needs;

    @Setter
    private String taskName;

    @Setter
    private String taskDescription;

    @Setter
    private LocalDateTime startTimeDate;

    @Setter
    private LocalDateTime estimatedEndTimeDate;

    @Setter
    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Setter
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Setter
    @Enumerated(EnumType.STRING)
    private EmergencyLevel emergencyLevel;

    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @Setter
    @ManyToMany(mappedBy = "tasks", fetch = FetchType.EAGER)
    private List<Volunteer> volunteers;

    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    private Zone zone;

    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    private Notification notification;

    private LocalDate creationDate;

    public Task(List<Need> needs, String taskName, String taskDescription, LocalDateTime startTimeDate,
                LocalDateTime estimatedEndTimeDate, Priority priority, EmergencyLevel emergencyLevel, Status status, List<Volunteer> volunteers) {
        this.needs = needs;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.startTimeDate = startTimeDate;
        this.estimatedEndTimeDate = estimatedEndTimeDate;
        this.priority = priority;
        this.emergencyLevel = emergencyLevel;
        this.status = status;
        this.volunteers= volunteers;
        this.type = needs.getFirst().getTaskType();
        this.creationDate = LocalDate.now();
    }
}
