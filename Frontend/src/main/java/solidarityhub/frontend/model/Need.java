package solidarityhub.frontend.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.enums.TaskType;
import solidarityhub.frontend.model.enums.UrgencyLevel;

@Getter
@NoArgsConstructor
public class Need {
    private int id;

    @Setter
    private String description;

    @Setter
    private UrgencyLevel urgency;

    @Setter
    private TaskType taskType;

    @Setter
    private GPSCoordinates location;

    @Setter
    private Catastrophe catastrophe;

    @Setter
    private Task task;


    public Need(String description, UrgencyLevel urgency, TaskType taskType, GPSCoordinates location, Catastrophe catastrophe) {
        this.description = description;
        this.urgency = urgency;
        this.taskType = taskType;
        this.location = location;
        this.catastrophe = catastrophe;
    }
}
