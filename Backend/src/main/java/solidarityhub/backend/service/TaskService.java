package solidarityhub.backend.service;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    public TaskService(TaskRepository taskRepository) {this.taskRepository = taskRepository;}
    public Task saveTask(Task task) {return this.taskRepository.save(task);}
    public List<Task> getAllTasks() {return this.taskRepository.findAll();}
    public Task getTaskById(Integer id) {return this.taskRepository.findById(id).get();}
    public void deleteTask(Task task) {this.taskRepository.delete(task);}
}
