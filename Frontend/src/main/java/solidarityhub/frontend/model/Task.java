package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import org.pingu.domain.enums.TaskType;

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
    private TaskType type;

    @Setter
    private Priority priority;

    @Setter
    private EmergencyLevel emergencyLevel;

    @Setter
    private Status status;

    @Setter
    private List<Volunteer> volunteers;

    @Setter
    private String meetingDirection;

    @Setter
    private List<ResourceAssignment> resourceAssignments;

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
