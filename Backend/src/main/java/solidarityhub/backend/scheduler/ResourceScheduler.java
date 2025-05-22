package solidarityhub.backend.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import solidarityhub.backend.model.Notification;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.ResourceAssignment;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.observer.impl.ResourceObservable;
import solidarityhub.backend.observer.impl.ResourceObserver;
import solidarityhub.backend.observer.impl.StorageObservable;
import solidarityhub.backend.observer.impl.TaskObservable;
import solidarityhub.backend.repository.NotificationRepository;
import solidarityhub.backend.repository.ResourceRepository;
import solidarityhub.backend.repository.StorageRepository;
import solidarityhub.backend.repository.TaskRepository;
import solidarityhub.backend.service.NotificationService;

@Component
@EnableScheduling
public class ResourceScheduler {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    private final ResourceObservable resourceObservable;
    private final StorageObservable storageObservable;
    private final TaskObservable taskObservable;

    public ResourceScheduler() {
        this.resourceObservable = new ResourceObservable(null);
        this.storageObservable = new StorageObservable(null);
        this.taskObservable = new TaskObservable();
    }


    public void initObservers(ResourceObserver resourceObserver) {
        resourceObservable.addObserver(resourceObserver);
        storageObservable.addObserver(resourceObserver);
        taskObservable.addObserver(resourceObserver);
    }

    // Check resources every hour
    @Scheduled(fixedRate = 3600000)
    public void checkResources() {
        List<Resource> resources = resourceRepository.findAll();

        for (Resource resource : resources) {
            if (resource.isBelowThreshold()) {
                createLowResourceNotification(resource);
            }

            resourceObservable.resourceUpdated(resource);
        }
    }

    // Check storage capacity every hour
    @Scheduled(fixedRate = 3600000)
    public void checkStorageCapacity() {
        List<Storage> storages = storageRepository.findAll();

        for (Storage storage : storages) {
            if (storage.getCapacityUsagePercentage() >= 90.0) {
                createStorageCapacityNotification(storage);
            }

            storageObservable.setStorage(storage);
        }
    }

    // Check task deadlines more frequently (every 15 minutes)
    @Scheduled(fixedRate = 900000)
    public void checkTaskDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = taskRepository.findAll();

        for (Task task : tasks) {
            // Check if task should transition from TO_DO to IN_PROGRESS
            if (task.getStatus() == Status.TO_DO && task.getStartTimeDate().isBefore(now)) {
                task.setStatus(Status.IN_PROGRESS);
                taskRepository.save(task);
                createTaskStatusChangeNotification(task, "ha cambiado a EN PROGRESO");
            }

            // Check if task should transition from IN_PROGRESS to FINISHED
            if (task.getStatus() == Status.IN_PROGRESS &&
                    task.getEstimatedEndTimeDate() != null &&
                    task.getEstimatedEndTimeDate().isBefore(now)) {

                // Process resource consumption
                consumeResourcesForCompletedTask(task);

                // Update task status
                task.setStatus(Status.FINISHED);
                taskRepository.save(task);
                createTaskStatusChangeNotification(task, "ha sido completada");
            }

            taskObservable.setTask(task);
        }
    }

    // Helper method to process resource consumption when a task is completed
    private void consumeResourcesForCompletedTask(Task task) {
        // AGREGAR VERIFICACIÓN DE NULL
        List<ResourceAssignment> assignments = task.getResourceAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        for (ResourceAssignment assignment : assignments) {
            Resource resource = assignment.getResource();
            if (resource != null) {
                double assignedQuantity = assignment.getQuantity();

                // Subtract the used quantity from the resource
                double newQuantity = resource.getQuantity() - assignedQuantity;
                double oldQuantity = resource.getQuantity();

                // Ensure we don't go below zero
                newQuantity = Math.max(0, newQuantity);

                // Update the resource quantity
                resource.setQuantity(newQuantity);
                resource.setCantidad(resource.getQuantity() + " " + resource.getUnit());

                // Save the updated resource
                resourceRepository.save(resource);

                // Check if the resource is now below threshold after consumption
                if (oldQuantity >= Resource.MINIMUM_RESOURCE_THRESHOLD &&
                        newQuantity < Resource.MINIMUM_RESOURCE_THRESHOLD) {
                    createLowResourceNotification(resource);
                }
            }
        }
    }

    // Helper methods to create notifications
    private void createLowResourceNotification(Resource resource) {
        String title = "Nivel bajo de recurso: " + resource.getName();
        String body = "El recurso " + resource.getName() + " ha bajado del umbral mínimo. " +
                "Cantidad actual: " + resource.getQuantity() + " " + resource.getUnit();

        Notification notification = new Notification(title, body, null, null);
        notification.setSeen(false);
        notificationRepository.save(notification);
    }

    private void createStorageCapacityNotification(Storage storage) {
        String title = "Almacén casi lleno: " + storage.getName();
        String body = "El almacén " + storage.getName() + " está alcanzando su capacidad máxima. " +
                "Uso actual: " + String.format("%.1f", storage.getCapacityUsagePercentage()) + "%. " +
                "Considere redirigir recursos a otro almacén.";

        Notification notification = new Notification(title, body, null, null);
        notification.setSeen(false);
        notificationRepository.save(notification);
    }

    private void createTaskStatusChangeNotification(Task task, String statusMessage) {
        String title = "Estado de tarea actualizado: " + task.getTaskName();
        String body = "La tarea '" + task.getTaskName() + "' " + statusMessage + " automáticamente.";

        // Create notification for system (visible to all)
        Notification systemNotification = new Notification(title, body, task, null);
        systemNotification.setSeen(false);
        notificationRepository.save(systemNotification);

        // Create notification for each volunteer assigned to the task
        if (task.getVolunteers() != null) {
            for (Volunteer volunteer : task.getVolunteers()) {
                Notification notification = new Notification(title, body, task, volunteer);
                notification.setSeen(false);
                notificationRepository.save(notification);

                // Send email and app notification if tokens are available
                if (volunteer.getEmail() != null) {
                    notificationService.notifyEmail(volunteer.getEmail(), notification);
                }
                if (volunteer.getNotificationToken() != null) {
                    notificationService.notifyApp(volunteer.getNotificationToken(), title, body);
                }
            }
        }
    }
}