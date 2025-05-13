package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
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
    @ManyToMany(mappedBy = "tasks", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Volunteer> volunteers;

    @Setter
    @ManyToMany(mappedBy = "acceptedTasks", cascade = CascadeType.ALL)
    private List<Volunteer> acceptedVolunteers;

    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    private Zone zone;

    @Setter
    @ManyToOne
    @JoinColumn(name = "catastrophe_id")
    private Catastrophe catastrophe;

    private final LocalDate creationDate;

    @Setter
    private String meetingDirection;

    @Setter
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PDFCertificate> certificates;

    public Task(List<Need> needs, String taskName, String taskDescription, LocalDateTime startTimeDate,
                LocalDateTime estimatedEndTimeDate, Priority priority, EmergencyLevel emergencyLevel, Status status,
                List<Volunteer> volunteers, String meetingDirection) {
        this.certificates = new ArrayList<>();
        this.notifications = new ArrayList<>();
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
        this.meetingDirection = meetingDirection;
    }
    public Task(List<Need> needs, String taskName, String taskDescription, LocalDateTime startTimeDate,
                LocalDateTime estimatedEndTimeDate, Priority priority, EmergencyLevel emergencyLevel,
                Status status, List<Volunteer> volunteers, String meetingDirection, Catastrophe catastrophe) {
        this(needs, taskName, taskDescription, startTimeDate, estimatedEndTimeDate, priority, emergencyLevel, status, volunteers, meetingDirection);
        this.catastrophe = catastrophe;
    }

    public Task(){
        this.creationDate = LocalDate.now();
        this.notifications = new ArrayList<>();
        this.certificates = new ArrayList<>();
    }

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }
}
