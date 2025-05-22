package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solidarityhub.backend.criteria.tasks.*;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.builder.*;
import solidarityhub.backend.model.enums.*;
import solidarityhub.backend.observer.impl.TaskObservable;
import solidarityhub.backend.observer.impl.ResourceObserver;
import solidarityhub.backend.repository.TaskRepository;
import solidarityhub.backend.repository.NotificationRepository;
import solidarityhub.backend.repository.ResourceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final VolunteerService volunteerService;
    private final TaskObservable taskObservable;
    private final ResourceObserver resourceObserver;

    private final Map<UrgencyLevel, Supplier<TaskBuilder>> builderRegistry;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       VolunteerService volunteerService,
                       NotificationService notificationService,
                       NotificationRepository notificationRepository,
                       ResourceRepository resourceRepository,
                       ResourceService resourceService) {
        this.taskRepository = taskRepository;
        this.volunteerService = volunteerService;
        this.taskObservable = new TaskObservable();

        // Crear el observer y configurarlo
        this.resourceObserver = new ResourceObserver(
                notificationService,
                notificationRepository,
                resourceRepository,
                taskRepository,
                resourceService,
                this
        );

        // Agregar el observer al observable
        this.taskObservable.addObserver(resourceObserver);

        builderRegistry = Map.of(
                UrgencyLevel.URGENT, () -> new UrgentTaskBuilder(volunteerService),
                UrgencyLevel.MODERATE, () -> new ModerateTaskBuilder(volunteerService),
                UrgencyLevel.LOW, () -> new LowPriorityTaskBuilder(volunteerService)
        );
    }

    public Task save(Task task) {
        Task savedTask = taskRepository.save(task);
        // Notificar al observable sobre el cambio
        taskObservable.setTask(savedTask);
        return savedTask;
    }

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

    public Integer getToDoTasksCount(Integer catastropheId) {
        return taskRepository.getTasksByStatusCount(catastropheId, Status.TO_DO);
    }

    public Integer getInProgressTasksCount(Integer catastropheId) {
        return taskRepository.getTasksByStatusCount(catastropheId, Status.IN_PROGRESS);
    }

    public Integer getFinishedTasksCount(Integer catastropheId) {
        return taskRepository.getTasksByStatusCount(catastropheId, Status.FINISHED);
    }

    public List<Task> filter(String status, String priority, String type, String emergencyLevel, Integer catastropheId) {
        List<Task> tasks = taskRepository.getTasksByCatastrophe(catastropheId);
        TaskFilter filter = null;

        if (status != null && !status.isEmpty()) {
            filter = new StatusFilter(Status.valueOf(status));
        }
        if (priority != null && !priority.isEmpty()) {
            if (filter != null) filter = new AndFilter(filter, new PriorityFilter(Priority.valueOf(priority)));
            else filter = new PriorityFilter(Priority.valueOf(priority));
        }
        if (type != null && !type.isEmpty()) {
            if (filter != null) filter = new AndFilter(filter, new TypeFilter(TaskType.valueOf(type)));
            else filter = new TypeFilter(TaskType.valueOf(type));
        }
        if (emergencyLevel != null && !emergencyLevel.isEmpty()) {
            if (filter != null) filter = new AndFilter(filter, new EmergencyLevelFilter(EmergencyLevel.valueOf(emergencyLevel)));
            else filter = new EmergencyLevelFilter(EmergencyLevel.valueOf(emergencyLevel));
        }

        if (filter != null) {
            tasks = filter.filter(tasks);
        }

        return tasks;
    }

    public void checkAllTaskDeadlines() {
        List<Task> tasks = taskRepository.findAll();
        for (Task task : tasks) {
            taskObservable.setTask(task);
        }
    }

    // Método específico para actualizar el estado de una tarea
    public void updateTaskStatus(Task task) {
        Task savedTask = taskRepository.save(task);
        taskObservable.setTask(savedTask);
    }
}
