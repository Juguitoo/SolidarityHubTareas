package solidarityhub.frontend.dto;

import lombok.Getter;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.model.Volunteer;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class VolunteerDTO {
    private String dni;
    private String firstName;
    private String lastName;
    private String email;
    private List<Integer> tasks;
    private List<TaskType> taskTypes;
    private int availabilityStatus;

    public VolunteerDTO(Volunteer volunteer) {
        this.tasks = new ArrayList<>();
        this.dni = volunteer.getDni();
        this.firstName = volunteer.getFirstName();
        this.lastName = volunteer.getLastName();
        this.email = volunteer.getEmail();
        this.taskTypes = volunteer.getTaskTypes();
        volunteer.getTasks().forEach(t ->{tasks.add(t.getId());});
    }
    public int isAvailable() {
        return availabilityStatus;
    }
}
