package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.TaskType;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class VolunteerDTO {
    private String dni;
    private String firstName;
    private String lastName;
    private String email;
    private List<Integer> tasks;
    private List<TaskType> taskTypes;

    public VolunteerDTO(Volunteer volunteer) {
        this.tasks = new ArrayList<>();
        this.dni = volunteer.getDni();
        this.firstName = volunteer.getFirstName();
        this.lastName = volunteer.getLastName();
        this.email = volunteer.getEmail();
        this.taskTypes = volunteer.getTaskTypes();
        volunteer.getTasks().forEach(t ->{tasks.add(t.getId());});
    }
}
