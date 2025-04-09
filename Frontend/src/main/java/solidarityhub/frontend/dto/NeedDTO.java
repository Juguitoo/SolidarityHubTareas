package solidarityhub.frontend.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    private GPSCoordinates location;
    private int taskId;
    private int catastropheId; // Añadido para saber a qué catástrofe pertenece la necesidad

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

    // Constructor para uso en pruebas o ejemplos
    public NeedDTO(int id, String description, UrgencyLevel urgency, TaskType taskType,
                   GPSCoordinates location, int taskId, int catastropheId) {
        this.id = id;
        this.description = description;
        this.urgency = urgency;
        this.taskType = taskType;
        this.location = location;
        this.taskId = taskId;
        this.catastropheId = catastropheId;
    }

    public int getCatastropheId() {
        return this.catastropheId;
    }
}
