package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.NeedType;
import solidarityhub.backend.model.enums.Priority;
import solidarityhub.backend.model.enums.Status;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    @OneToMany(mappedBy = "task")
    private Set<Need> need;

    @Setter
    private String taskName;

    @Setter
    private String taskDescription;

    @Setter
    private LocalDateTime startTimeDate;

    @Setter
    private LocalDateTime estimatedEndTimeDate;

    @Setter
    private NeedType type;

    @Setter
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @Setter
    @ManyToMany(mappedBy = "tasks")
    private Set<Volunteer> volunteers;

    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    private Zone zone;

    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    private Notification notification;


    public Task(Need need, String taskName, String taskDescription, LocalDateTime startTimeDate,
                LocalDateTime estimatedEndTimeDate, Priority priority, Status status, Volunteer volunteer) {
        this.need = Set.of(need);
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.startTimeDate = startTimeDate;
        this.estimatedEndTimeDate = estimatedEndTimeDate;
        this.priority = priority;
        this.status = status;
        this.volunteers= Set.of(volunteer);
        volunteer.getTasks().add(this);
        this.type = need.getNeedType();
    }
}
