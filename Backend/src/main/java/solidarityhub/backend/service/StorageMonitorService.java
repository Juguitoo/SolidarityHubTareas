package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import solidarityhub.backend.model.Storage;
import solidarityhub.backend.observer.impl.StorageObservable;
import solidarityhub.backend.observer.impl.ResourceObserver;
import solidarityhub.backend.repository.NotificationRepository;
import solidarityhub.backend.repository.ResourceRepository;
import solidarityhub.backend.repository.StorageRepository;
import solidarityhub.backend.repository.TaskRepository;

import java.util.List;

@Service
public class StorageMonitorService {

    private final StorageRepository storageRepository;
    private final StorageObservable storageObservable;
    private final ResourceObserver resourceObserver;

    @Autowired
    public StorageMonitorService(StorageRepository storageRepository,
                                 NotificationService notificationService,
                                 NotificationRepository notificationRepository,
                                 ResourceRepository resourceRepository,
                                 TaskRepository taskRepository,
                                 ResourceService resourceService,
                                 TaskService taskService) {
        this.storageRepository = storageRepository;
        this.resourceObserver = new ResourceObserver(
                notificationService,
                notificationRepository,
                resourceRepository,
                taskRepository,
                resourceService,
                taskService
        );
        this.storageObservable = new StorageObservable(null);

        // Add observer to observable
        storageObservable.addObserver(resourceObserver);
    }

    // Method to monitor a specific storage
    public void checkStorage(Storage storage) {
        storageObservable.setStorage(storage);
    }

    // Method to check all storages
    public void checkAllStorages() {
        List<Storage> storages = storageRepository.findAll();
        for (Storage storage : storages) {
            checkStorage(storage);
        }
    }

    // Method called when a resource is added to storage
    public void resourceAdded(Storage storage) {
        storageObservable.resourceAdded(storage);
    }
}
