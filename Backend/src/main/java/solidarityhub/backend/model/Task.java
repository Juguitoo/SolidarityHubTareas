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

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<PDFCertificate> certificates;

    @Setter
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<ResourceAssignment> resourceAssignments;

    public Task(List<Need> needs, String taskName, String taskDescription, LocalDateTime startTimeDate,
                LocalDateTime estimatedEndTimeDate, Priority priority, EmergencyLevel emergencyLevel, Status status,
                List<Volunteer> volunteers, String meetingDirection) {
        // INICIALIZAR TODAS LAS LISTAS
        initializeLists();

        this.needs = needs != null ? needs : new ArrayList<>();
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.startTimeDate = startTimeDate;
        this.estimatedEndTimeDate = estimatedEndTimeDate;
        this.priority = priority;
        this.emergencyLevel = emergencyLevel;
        this.status = status;
        this.volunteers = volunteers != null ? volunteers : new ArrayList<>();
        this.type = (needs != null && !needs.isEmpty()) ? needs.get(0).getTaskType() : null;
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
        initializeLists();
    }

    // MÉTODO PRIVADO PARA INICIALIZAR TODAS LAS LISTAS
    private void initializeLists() {
        this.notifications = new ArrayList<>();
        this.certificates = new ArrayList<>();
        this.resourceAssignments = new ArrayList<>();
        this.acceptedVolunteers = new ArrayList<>();
        this.volunteers = new ArrayList<>();
        this.needs = new ArrayList<>();
    }

    public void addNotification(Notification notification) {
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }
        this.notifications.add(notification);
    }

    public void addResourceAssignment(ResourceAssignment assignment) {
        if (this.resourceAssignments == null) {
            this.resourceAssignments = new ArrayList<>();
        }
        this.resourceAssignments.add(assignment);
    }

    // GETTERS SEGUROS QUE VERIFICAN NULL
    public List<ResourceAssignment> getResourceAssignments() {
        if (this.resourceAssignments == null) {
            this.resourceAssignments = new ArrayList<>();
        }
        return this.resourceAssignments;
    }

    public List<Volunteer> getAcceptedVolunteers() {
        if (this.acceptedVolunteers == null) {
            this.acceptedVolunteers = new ArrayList<>();
        }
        return this.acceptedVolunteers;
    }

    public List<Volunteer> getVolunteers() {
        if (this.volunteers == null) {
            this.volunteers = new ArrayList<>();
        }
        return this.volunteers;
    }

    public List<Need> getNeeds() {
        if (this.needs == null) {
            this.needs = new ArrayList<>();
        }
        return this.needs;
    }

    public List<Notification> getNotifications() {
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }
        return this.notifications;
    }

    public List<PDFCertificate> getCertificates() {
        if (this.certificates == null) {
            this.certificates = new ArrayList<>();
        }
        return this.certificates;
    }
}
