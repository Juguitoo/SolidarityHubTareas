package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.NeedType;
import solidarityhub.backend.model.enums.Priority;
import solidarityhub.backend.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class TaskDTO {
    private int id;
    private String name;
    private String description;
    private LocalDateTime startTimeDate;
    private LocalDateTime estimatedEndTimeDate;
    private NeedType type;
    private Priority priority;
    private EmergencyLevel emergencyLevel;
    private Status status;
    private List<NeedDTO> needs;
    private List<VolunteerDTO> volunteers;

    public TaskDTO(Task task) {
        this.needs = new ArrayList<>();
        this.volunteers = new ArrayList<>();
        this.id = task.getId();
        this.name = task.getTaskName();
        this.description = task.getTaskDescription();
        this.startTimeDate = task.getStartTimeDate();
        this.estimatedEndTimeDate = task.getEstimatedEndTimeDate();
        this.type = task.getType();
        this.priority = task.getPriority();
        this.emergencyLevel = task.getEmergencyLevel();
        this.status = task.getStatus();
        task.getNeeds().forEach(n -> {needs.add(new NeedDTO(n));});
        task.getVolunteers().forEach(v -> {volunteers.add(new VolunteerDTO(v));});
    }
}
