package solidarityhub.backend.criteria.tasks;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.enums.EmergencyLevel;

import java.util.List;

public class EmergencyLevelFilter implements TaskFilter {
    private final EmergencyLevel emergencyLevel;

    public EmergencyLevelFilter(EmergencyLevel emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getEmergencyLevel().equals(emergencyLevel))
                .toList();
    }
}
