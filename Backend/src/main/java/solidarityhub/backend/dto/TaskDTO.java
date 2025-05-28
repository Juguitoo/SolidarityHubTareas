package solidarityhub.backend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.enums.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        if (task.getCatastrophe() != null) {
            this.catastropheId = task.getCatastrophe().getId();
        }
        this.meetingDirection = task.getMeetingDirection();
    }

    @JsonCreator
    public TaskDTO(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("startTimeDate") LocalDateTime startTimeDate,
            @JsonProperty("estimatedEndTimeDate") LocalDateTime estimatedEndTimeDate,
            @JsonProperty("type") TaskType type,
            @JsonProperty("priority") Priority priority,
            @JsonProperty("emergencyLevel") EmergencyLevel emergencyLevel,
            @JsonProperty("status") Status status,
            @JsonProperty("needs") List<NeedDTO> needs,
            @JsonProperty("volunteers") List<VolunteerDTO> volunteers,
            @JsonProperty("catastropheId") Integer catastropheId,
            @JsonProperty("meetingDirection") String meetingDirection
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startTimeDate = startTimeDate;
        this.estimatedEndTimeDate = estimatedEndTimeDate;
        this.type = type;
        this.priority = priority;
        this.emergencyLevel = emergencyLevel;
        this.status = status;
        this.needs = needs != null ? needs : new ArrayList<>();
        this.volunteers = volunteers != null ? volunteers : new ArrayList<>();
        this.meetingDirection = meetingDirection;
        this.catastropheId = catastropheId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskDTO)) return false;

        TaskDTO taskDTO = (TaskDTO) o;

        if (id != taskDTO.id) return false;
        if (!name.equals(taskDTO.name)) return false;
        if (!description.equals(taskDTO.description)) return false;
        if (!startTimeDate.equals(taskDTO.startTimeDate)) return false;
        if (!estimatedEndTimeDate.equals(taskDTO.estimatedEndTimeDate)) return false;
        if (type != taskDTO.type) return false;
        if (priority != taskDTO.priority) return false;
        if (emergencyLevel != taskDTO.emergencyLevel) return false;
        if (status != taskDTO.status) return false;
        if (!needs.equals(taskDTO.needs)) return false;
        if (!volunteers.equals(taskDTO.volunteers)) return false;
        if (!meetingDirection.equals(taskDTO.meetingDirection)) return false;
        return catastropheId != null ? catastropheId.equals(taskDTO.catastropheId) : taskDTO.catastropheId == null;
    }
}
