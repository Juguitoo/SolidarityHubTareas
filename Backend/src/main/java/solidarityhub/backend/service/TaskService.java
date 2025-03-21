package solidarityhub.backend.service;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    public TaskService(TaskRepository taskRepository) {this.taskRepository = taskRepository;}
    public Task saveTask(Task task) {return this.taskRepository.save(task);}
    public List<Task> getTasks() {return this.taskRepository.findAll();}

}
