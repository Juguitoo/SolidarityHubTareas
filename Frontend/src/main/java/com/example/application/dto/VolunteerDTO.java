package com.example.application.dto;

import lombok.Getter;
import com.example.application.model.Volunteer;
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

    public VolunteerDTO(Volunteer volunteer) {
        this.tasks = new ArrayList<>();
        this.dni = volunteer.getDni();
        this.firstName = volunteer.getFirstName();
        this.lastName = volunteer.getLastName();
        this.email = volunteer.getEmail();
        volunteer.getTasks().forEach(t ->{tasks.add(t.getId());});
    }
}
