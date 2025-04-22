package solidarityhub.frontend.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.model.enums.TaskType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class Task {
    private int id;

    @Setter
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
    private List<Volunteer> volunteers;

    @Setter
    private String meetingDirection;

    public Task(List<Need> needs, String taskName, String taskDescription, LocalDateTime startTimeDate,
                LocalDateTime estimatedEndTimeDate, Priority priority, EmergencyLevel emergencyLevel, Status status, List<Volunteer> volunteers, String meetingDirection) {
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
        this.meetingDirection = meetingDirection;
    }
}
