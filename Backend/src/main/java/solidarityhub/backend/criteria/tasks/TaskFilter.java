package solidarityhub.backend.criteria.tasks;

import solidarityhub.backend.model.Task;

import java.util.List;

public interface TaskFilter {
    List<Task> filter(List<Task> tasks);
}
