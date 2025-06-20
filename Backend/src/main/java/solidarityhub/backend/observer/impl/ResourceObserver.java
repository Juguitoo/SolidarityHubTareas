package solidarityhub.backend.observer.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.observer.Observable;
import solidarityhub.backend.observer.Observer;
import solidarityhub.backend.repository.NotificationRepository;
import solidarityhub.backend.repository.ResourceRepository;
import solidarityhub.backend.repository.TaskRepository;
import solidarityhub.backend.service.NotificationService;
import solidarityhub.backend.service.ResourceService;
import solidarityhub.backend.service.TaskService;

@Component
public class ResourceObserver implements Observer {

    private final NotificationRepository notificationRepository;
    private final ResourceRepository resourceRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public ResourceObserver(NotificationService notificationService,
                            NotificationRepository notificationRepository,
                            ResourceRepository resourceRepository,
                            TaskRepository taskRepository,
                            ResourceService resourceService,
                            TaskService taskService) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.resourceRepository = resourceRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof ResourceObservable && arg instanceof Resource) {
            Resource resource = (Resource) arg;
            checkResourceLevel(resource);
        } else if (observable instanceof StorageObservable && arg instanceof Storage) {
            Storage storage = (Storage) arg;
            checkStorageCapacity(storage);
        } else if (observable instanceof TaskObservable) {
            // Obtener la tarea del observable
            TaskObservable taskObservable = (TaskObservable) observable;
            Task task = taskObservable.getTask();
            if (task != null) {
                checkTaskStatus(task);
            }
        }
    }

    private void checkResourceLevel(Resource resource) {
        if (resource.isBelowThreshold()) {
            createLowResourceNotification(resource);
        }
    }

    private void checkStorageCapacity(Storage storage) {
        if (storage.getCapacityUsagePercentage() >= 90.0) {
            createStorageCapacityNotification(storage);
        }
    }

    private void checkTaskStatus(Task task) {
        LocalDateTime now = LocalDateTime.now();

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

            // Process resource consumption - VERIFICACIONES COMPLETAS DE NULL
            List<ResourceAssignment> assignments = task.getResourceAssignments();
            if (assignments != null && !assignments.isEmpty()) {
                assignments.forEach(assignment -> {
                    Resource resource = assignment.getResource();
                    if (resource != null) {
                        double assignedQuantity = assignment.getQuantity();

                        // Subtract the used quantity from the resource
                        double newQuantity = resource.getQuantity() - assignedQuantity;

                        // Update the resource quantity
                        resource.updateQuantity(Math.max(0, newQuantity));

                        // Save the updated resource
                        resourceRepository.save(resource);

                        // Check if resource is now below threshold
                        if (resource.isBelowThreshold()) {
                            createLowResourceNotification(resource);
                        }
                    }
                });
            }

            // Update task status
            task.setStatus(Status.FINISHED);
            taskRepository.save(task);
            createTaskStatusChangeNotification(task, "ha sido completada");
        }
    }

    private String getStatusMessage(Status status) {
        return switch (status) {
            case TO_DO -> "está pendiente";
            case IN_PROGRESS -> "está en progreso";
            case FINISHED -> "ha sido completada";
        };
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
        String body = "La tarea '" + task.getTaskName() + "' " + statusMessage + ".";

        // Create notification for system (visible to all)
        Notification systemNotification = new Notification(title, body, task, null);
        systemNotification.setSeen(false);
        notificationRepository.save(systemNotification);

        // Create notification for each volunteer assigned to the task
        List<Volunteer> volunteers = task.getVolunteers();
        if (volunteers != null && !volunteers.isEmpty()) {
            for (Volunteer volunteer : volunteers) {
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