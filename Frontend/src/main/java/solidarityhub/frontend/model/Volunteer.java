package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pingu.domain.enums.TaskType;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class Volunteer extends Person {

    private List<Task> tasks;
    private List<TaskType> taskTypes;

    public Volunteer(String dni, String firstName, String lastName, String email) {
        super(dni, firstName, lastName, email);
        this.tasks = new ArrayList<>();
        this.taskTypes = new ArrayList<>();
    }
}
