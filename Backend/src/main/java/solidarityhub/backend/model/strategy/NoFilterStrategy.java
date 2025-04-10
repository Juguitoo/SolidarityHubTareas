package solidarityhub.backend.model.strategy;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Volunteer;

import java.util.List;

public class NoFilterStrategy implements VolunteerSelectionStrategy{
    @Override
    public List<Volunteer> selectVolunteers(List<Volunteer> volunteers, TaskDTO taskDTO) {
        return volunteers;
    }
}
