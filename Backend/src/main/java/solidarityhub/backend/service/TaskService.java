package solidarityhub.backend.service;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    public TaskService(TaskRepository taskRepository) {this.taskRepository = taskRepository;}
    public Task saveTask(Task task) {return this.taskRepository.save(task);}
}
