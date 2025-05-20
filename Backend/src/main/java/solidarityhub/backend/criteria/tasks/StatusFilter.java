package solidarityhub.backend.criteria.tasks;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.enums.Status;

import java.util.List;

public class StatusFilter implements TaskFilter {
    private final Status status;

    public StatusFilter(Status status) {
        this.status = status;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getStatus().equals(status))
                .toList();
    }
}
