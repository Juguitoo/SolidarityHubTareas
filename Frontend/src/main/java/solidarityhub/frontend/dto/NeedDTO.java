package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import solidarityhub.frontend.model.GPSCoordinates;
import solidarityhub.frontend.model.Need;
import solidarityhub.frontend.model.enums.TaskType;
import solidarityhub.frontend.model.enums.UrgencyLevel;

@NoArgsConstructor
@Getter
public class NeedDTO {
    private int id;
    private String description;
    private UrgencyLevel urgency;
    private TaskType taskType;
    private GPSCoordinates location;
    private int taskId;

    public NeedDTO(Need need) {
        this.id = need.getId();
        this.description = need.getDescription();
        this.urgency = need.getUrgency();
        this.taskType = need.getTaskType();
        this.location = need.getLocation();
        if(need.getTask() != null) {
            this.taskId = need.getTask().getId();
        } else {
            this.taskId = -1;
        }
    }
}
