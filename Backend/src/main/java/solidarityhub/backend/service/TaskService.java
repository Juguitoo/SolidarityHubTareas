package solidarityhub.backend.service;

import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.builder.*;
import solidarityhub.backend.model.enums.UrgencyLevel;
import solidarityhub.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<Task> getSuggestedTasks(List<Need> needs) {
        List<Task> suggestedTasks = new ArrayList<>();
        TaskDirector taskDirector = new TaskDirector();
        TaskBuilder taskBuilder;
        for (Need need : needs) {
            if (need.getUrgency() == UrgencyLevel.URGENT){
                taskBuilder = new UrgentTask(need);
                taskDirector.construct(taskBuilder);
                suggestedTasks.add(taskBuilder.getResult());
            }else if(need.getUrgency() == UrgencyLevel.MODERATE) {
                taskBuilder = new ModerateTask(need);
                taskDirector.construct(taskBuilder);
                suggestedTasks.add(taskBuilder.getResult());
            }else if(need.getUrgency() == UrgencyLevel.LOW) {
                taskBuilder = new LowPriorityTask(need);
                taskDirector.construct(taskBuilder);
                suggestedTasks.add(taskBuilder.getResult());
            }
        }
        return suggestedTasks;
    }
}
