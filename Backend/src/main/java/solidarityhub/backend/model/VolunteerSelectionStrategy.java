package solidarityhub.backend.model;

import solidarityhub.backend.dto.TaskDTO;

import java.util.List;

public interface VolunteerSelectionStrategy {
    List<Volunteer> selectVolunteers(List<Volunteer> volunteers, TaskDTO taskDTO);
}
