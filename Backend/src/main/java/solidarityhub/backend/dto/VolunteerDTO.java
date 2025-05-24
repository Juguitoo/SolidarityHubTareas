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
    private int availabilityStatus;

    public VolunteerDTO(Volunteer volunteer) {
        this.availabilityStatus = 0;
        this.tasks = new ArrayList<>();
        this.dni = volunteer.getDni();
        this.firstName = volunteer.getFirstName();
        this.lastName = volunteer.getLastName();
        this.email = volunteer.getEmail();
        this.taskTypes = volunteer.getTaskTypes();
        volunteer.getTasks().forEach(t ->{tasks.add(t.getId());});
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VolunteerDTO)) return false;

        VolunteerDTO that = (VolunteerDTO) o;

        return dni != null ? dni.equals(that.dni) : that.dni == null &&
               firstName != null ? firstName.equals(that.firstName) : that.firstName == null &&
               lastName != null ? lastName.equals(that.lastName) : that.lastName == null &&
               email != null ? email.equals(that.email) : that.email == null;
    }

}
