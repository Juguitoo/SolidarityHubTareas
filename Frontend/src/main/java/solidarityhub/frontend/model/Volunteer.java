package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class Volunteer extends Person {

    private List<Task> tasks;

    public Volunteer(String dni, String firstName, String lastName, String email) {
        super(dni, firstName, lastName, email);
        this.tasks = new ArrayList<>();
    }
}
