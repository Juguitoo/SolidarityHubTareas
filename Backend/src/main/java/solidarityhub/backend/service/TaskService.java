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
import java.util.Map;
import java.util.function.Supplier;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final VolunteerService volunteerService;

    private final Map<UrgencyLevel, Supplier<TaskBuilder>> builderRegistry;

    public TaskService(TaskRepository taskRepository, VolunteerService volunteerService) {
        this.taskRepository = taskRepository;
        this.volunteerService = volunteerService;
        builderRegistry = Map.of(
                UrgencyLevel.URGENT, () -> new UrgentTaskBuilder(volunteerService),
                UrgencyLevel.MODERATE, () -> new ModerateTaskBuilder(volunteerService),
                UrgencyLevel.LOW, () -> new LowPriorityTaskBuilder(volunteerService)
        );
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
            Supplier<TaskBuilder> builderSupplier = builderRegistry.get(need.getUrgency());

            if (builderSupplier == null)
                throw new IllegalArgumentException("No builder found for urgency level: " + need.getUrgency());

            taskBuilder = builderSupplier.get();
            taskBuilder.setNeed(need);
            taskDirector.construct(taskBuilder);
            suggestedTasks.add(taskBuilder.getResult());
        }
        return suggestedTasks;
    }

}
