package solidarityhub.backend.dto;

import lombok.Getter;
import solidarityhub.backend.model.Volunteer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class VolunteerDTO {
    private final String dni;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final List<Integer> tasks;

    public VolunteerDTO(Volunteer volunteer) {
        this.tasks = new ArrayList<>();
        this.dni = volunteer.getDni();
        this.firstName = volunteer.getFirstName();
        this.lastName = volunteer.getLastName();
        this.email = volunteer.getEmail();
        volunteer.getTasks().forEach(t ->{tasks.add(t.getId());});
    }
}
