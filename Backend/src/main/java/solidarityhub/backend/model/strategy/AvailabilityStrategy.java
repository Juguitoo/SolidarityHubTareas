package solidarityhub.backend.model.strategy;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Volunteer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AvailabilityStrategy implements VolunteerSelectionStrategy {
    @Override
    public List<Volunteer> selectVolunteers(List<Volunteer> volunteers, TaskDTO taskDTO) {
        return volunteers.stream()
                .sorted(Comparator.comparingInt((Volunteer v) ->
                        v.isAvailable(taskDTO.getStartTimeDate(), taskDTO.getEstimatedEndTimeDate())).reversed())
                .collect(Collectors.toList());
    }
}
