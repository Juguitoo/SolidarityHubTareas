package solidarityhub.backend.model.strategy;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Volunteer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DistanceStrategy implements VolunteerSelectionStrategy {
    @Override
    public List<Volunteer> selectVolunteers(List<Volunteer> volunteers, TaskDTO taskDTO) {
        return volunteers.stream()
                .sorted(Comparator.comparingDouble(v -> v.getDistance(taskDTO)))
                .collect(Collectors.toList());
    }

}
