package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import org.pingu.domain.enums.TaskType;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class TaskDTO {
    private int id;
    private String name;
    private String description;
    private LocalDateTime startTimeDate;
    private LocalDateTime estimatedEndTimeDate;
    private TaskType type;
    private Priority priority;
    private EmergencyLevel emergencyLevel;
    private Status status;
    private List<NeedDTO> needs;
    private List<VolunteerDTO> volunteers;
    private Integer catastropheId;
    private String meetingDirection;


    public TaskDTO(String name , String description, LocalDateTime startTimeDate, LocalDateTime estimatedEndTimeDate, TaskType type, Priority priority, EmergencyLevel emergencyLevel,
                   Status status, List<NeedDTO> needs, List<VolunteerDTO> volunteers, String meetingDirection) {
        this.needs = needs;
        this.volunteers = volunteers;
        this.name = name;
        this.description = description;
        this.startTimeDate = startTimeDate;
        this.estimatedEndTimeDate = estimatedEndTimeDate;
        this.type = type;
        this.priority = priority;
        this.emergencyLevel = emergencyLevel;
        this.status = status;
        this.catastropheId = null;
        this.meetingDirection = meetingDirection;
    }

    public TaskDTO(String name, String description, LocalDateTime startTimeDate,
                   LocalDateTime estimatedEndTimeDate, TaskType type, Priority priority,
                   EmergencyLevel emergencyLevel, Status status, List<NeedDTO> needs,
                   List<VolunteerDTO> volunteers, Integer catastropheId, String meetingDirection) {
        this(name, description, startTimeDate, estimatedEndTimeDate, type, priority,
                emergencyLevel, status, needs, volunteers, meetingDirection);
        this.catastropheId = catastropheId;
    }

}
