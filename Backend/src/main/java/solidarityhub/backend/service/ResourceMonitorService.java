package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import solidarityhub.backend.model.Resource;
import solidarityhub.backend.observer.impl.ResourceObservable;
import solidarityhub.backend.observer.impl.ResourceObserver;
import solidarityhub.backend.repository.NotificationRepository;
import solidarityhub.backend.repository.ResourceRepository;
import solidarityhub.backend.repository.TaskRepository;

import java.util.List;

@Service
public class ResourceMonitorService {

    private final ResourceRepository resourceRepository;
    private final ResourceObservable resourceObservable;
    private final ResourceObserver resourceObserver;

    @Autowired
    public ResourceMonitorService(ResourceRepository resourceRepository,
                                  NotificationService notificationService,
                                  NotificationRepository notificationRepository,
                                  TaskRepository taskRepository,
                                  ResourceService resourceService,
                                  TaskService taskService) {
        this.resourceRepository = resourceRepository;
        this.resourceObservable = new ResourceObservable(resourceRepository.findAll());
        this.resourceObserver = new ResourceObserver(
                notificationService,
                notificationRepository,
                resourceRepository,
                taskRepository,
                resourceService,
                taskService
        );

        // Add observer to observable
        resourceObservable.addObserver(resourceObserver);
    }

    // Method to manually check resources
    public void checkResources() {
        List<Resource> resources = resourceRepository.findAll();
        resourceObservable.setResources(resources);
    }

    // Method to update a single resource
    public void resourceUpdated(Resource resource) {
        resourceObservable.resourceUpdated(resource);
    }
}
