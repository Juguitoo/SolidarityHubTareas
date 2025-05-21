package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.observer.impl.TaskObservable;
import solidarityhub.backend.observer.impl.ResourceObserver;
import solidarityhub.backend.repository.NotificationRepository;
import solidarityhub.backend.repository.ResourceRepository;
import solidarityhub.backend.repository.TaskRepository;

import java.util.List;

@Service
public class TaskMonitorService {

    private final TaskRepository taskRepository;
    private final TaskObservable taskObservable;
    private final ResourceObserver resourceObserver;

    @Autowired
    public TaskMonitorService(TaskRepository taskRepository,
                              NotificationService notificationService,
                              NotificationRepository notificationRepository,
                              ResourceRepository resourceRepository,
                              ResourceService resourceService,
                              TaskService taskService) {
        this.taskRepository = taskRepository;
        this.resourceObserver = new ResourceObserver(
                notificationService,
                notificationRepository,
                resourceRepository,
                taskRepository,
                resourceService,
                taskService
        );
        this.taskObservable = new TaskObservable();

        // Add observer to observable
        taskObservable.addObserver(resourceObserver);
    }

    // Method to check a specific task
    public void checkTask(Task task) {
        taskObservable.setTask(task);
    }

    // Method to check all tasks
    @Scheduled(fixedRate = 3600000) // Check every hour
    public void checkAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        for (Task task : tasks) {
            checkTask(task);
        }
    }

    // Method to update status of a task
    public void updateTaskStatus(Task task) {
        taskObservable.setTask(task);
    }
}
