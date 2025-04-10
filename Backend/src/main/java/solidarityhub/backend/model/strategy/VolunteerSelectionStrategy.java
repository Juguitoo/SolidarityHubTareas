package solidarityhub.backend.model.strategy;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Volunteer;

import java.util.List;

public interface VolunteerSelectionStrategy {
    List<Volunteer> selectVolunteers(List<Volunteer> volunteers, TaskDTO taskDTO);
}
