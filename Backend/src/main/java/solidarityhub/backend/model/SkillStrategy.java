package solidarityhub.backend.model;

import solidarityhub.backend.dto.TaskDTO;

import java.util.List;
import java.util.stream.Collectors;

public class SkillStrategy implements VolunteerSelectionStrategy{
    @Override
    public List<Volunteer> selectVolunteers(List<Volunteer> volunteers, TaskDTO taskDTO) {
        return volunteers.stream()
                .filter(v -> v.getTaskTypes().contains(taskDTO.getType()))
                .collect(Collectors.toList());
    }
}
