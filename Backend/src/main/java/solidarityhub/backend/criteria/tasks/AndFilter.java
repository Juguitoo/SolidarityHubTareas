package solidarityhub.backend.criteria.tasks;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Task;

import java.util.List;

public class AndFilter implements TaskFilter {
    private final TaskFilter filter1;
    private final TaskFilter filter2;

    public AndFilter(TaskFilter filter1, TaskFilter filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    @Override
    public List<Task> filter(List<Task> tasks) {
        List<Task> filteredTasks1 = filter1.filter(tasks);
        return filter2.filter(filteredTasks1);
    }
}
