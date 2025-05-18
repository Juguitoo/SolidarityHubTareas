package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.builder.*;
import solidarityhub.backend.model.enums.UrgencyLevel;
import solidarityhub.backend.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final VolunteerService volunteerService;

    public TaskService(TaskRepository taskRepository, VolunteerService volunteerService) {
        this.taskRepository = taskRepository;
        this.volunteerService = volunteerService;
    }

    public Task save(Task task) {return this.taskRepository.save(task);}
    public List<Task> getAllTasks() {return this.taskRepository.findAll();}
    public Task getTaskById(Integer id) {return this.taskRepository.findById(id).get();}
    public void deleteTask(Task task) {this.taskRepository.delete(task);}
    public List<TaskDTO> getTasksByCatastropheId(Integer catastropheId) {
        return this.taskRepository.findAllByCatastropheId(catastropheId);
    }

    public List<Task> getSuggestedTasks(List<Need> needs) {
        List<Task> suggestedTasks = new ArrayList<>();
        TaskDirector taskDirector = new TaskDirector();
        TaskBuilder taskBuilder;
        for (Need need : needs) {
            if (need.getUrgency() == UrgencyLevel.URGENT){
                taskBuilder = new UrgentTaskBuilder(need, volunteerService);
                taskDirector.construct(taskBuilder);
                suggestedTasks.add(taskBuilder.getResult());

            }else if(need.getUrgency() == UrgencyLevel.MODERATE) {
                taskBuilder = new ModerateTaskBuilder(need, volunteerService);
                taskDirector.construct(taskBuilder);
                suggestedTasks.add(taskBuilder.getResult());

            }else if(need.getUrgency() == UrgencyLevel.LOW) {
                taskBuilder = new LowPriorityTaskBuilder(need, volunteerService);
                taskDirector.construct(taskBuilder);
                suggestedTasks.add(taskBuilder.getResult());
            }
        }
        return suggestedTasks;
    }

}
