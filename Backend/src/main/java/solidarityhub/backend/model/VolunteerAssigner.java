package solidarityhub.backend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.dto.TaskDTO;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class VolunteerAssigner {
    private VolunteerSelectionStrategy strategy;

    public VolunteerAssigner(VolunteerSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Volunteer> assignVolunteers(List<Volunteer> volunteers, TaskDTO taskDTO) {
        return strategy.selectVolunteers(volunteers, taskDTO);
    }
}
