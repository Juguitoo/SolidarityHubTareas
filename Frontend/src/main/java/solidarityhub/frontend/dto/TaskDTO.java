package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.model.enums.TaskType;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
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
    @Setter
    private Integer catastropheId;

    public TaskDTO(String name , String description, LocalDateTime startTimeDate, LocalDateTime estimatedEndTimeDate, TaskType type, Priority priority, EmergencyLevel emergencyLevel,
                   Status status, List<NeedDTO> needs, List<VolunteerDTO> volunteers) {
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
    }

    public TaskDTO(String name, String description, LocalDateTime startTimeDate,
                   LocalDateTime estimatedEndTimeDate, TaskType type, Priority priority,
                   EmergencyLevel emergencyLevel, Status status, List<NeedDTO> needs,
                   List<VolunteerDTO> volunteers, Integer catastropheId) {
        this(name, description, startTimeDate, estimatedEndTimeDate, type, priority,
                emergencyLevel, status, needs, volunteers);
        this.catastropheId = catastropheId;
    }

}
