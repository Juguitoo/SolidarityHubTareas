package solidarityhub.backend.criteria.tasks;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.enums.Priority;

import java.util.List;

public class PriorityFilter implements TaskFilter {
    private final Priority priority;

    public PriorityFilter(Priority priority) {
        this.priority = priority;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getPriority().equals(priority))
                .toList();
    }
}
