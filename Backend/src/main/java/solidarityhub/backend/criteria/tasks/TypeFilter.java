package solidarityhub.backend.criteria.tasks;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.enums.TaskType;

import java.util.List;

public class TypeFilter implements TaskFilter {
    private final TaskType type;

    public TypeFilter(TaskType type) {
        this.type = type;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getType().equals(type))
                .toList();
    }

}
