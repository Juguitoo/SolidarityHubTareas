package solidarityhub.backend.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.UrgencyLevel;

@NoArgsConstructor
@Getter
@Setter
public class NeedDTO {
    private int id;
    private String description;
    private UrgencyLevel urgency;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    private GPSCoordinates location;
    private int taskId;
    private int catastropheId;

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
        if(need.getCatastrophe() != null) {
            this.catastropheId = need.getCatastrophe().getId();
        } else {
            this.catastropheId = -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeedDTO)) return false;

        NeedDTO needDTO = (NeedDTO) o;

        if (id != needDTO.id) return false;
        if (taskId != needDTO.taskId) return false;
        if (catastropheId != needDTO.catastropheId) return false;
        if (!description.equals(needDTO.description)) return false;
        if (urgency != needDTO.urgency) return false;
        if (taskType != needDTO.taskType) return false;
        return true;
    }
}
