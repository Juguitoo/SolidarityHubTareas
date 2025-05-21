package solidarityhub.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import solidarityhub.backend.service.ResourceMonitorService;
import solidarityhub.backend.service.StorageMonitorService;
import solidarityhub.backend.service.TaskMonitorService;

@Configuration
@EnableScheduling
public class MonitoringScheduler {

    private final ResourceMonitorService resourceMonitorService;
    private final StorageMonitorService storageMonitorService;
    private final TaskMonitorService taskMonitorService;

    @Autowired
    public MonitoringScheduler(
            ResourceMonitorService resourceMonitorService,
            StorageMonitorService storageMonitorService,
            TaskMonitorService taskMonitorService) {
        this.resourceMonitorService = resourceMonitorService;
        this.storageMonitorService = storageMonitorService;
        this.taskMonitorService = taskMonitorService;
    }

    @Scheduled(fixedRate = 3600000) // Check every hour
    public void runSystemHealthCheck() {
        // Check all resources
        resourceMonitorService.checkResources();

        // Check all storages
        storageMonitorService.checkAllStorages();

        // Check all tasks
        taskMonitorService.checkAllTasks();
    }

    // Additional scheduled task specifically for checking task deadlines more frequently
    @Scheduled(fixedRate = 900000) // Check every 15 minutes
    public void checkTaskDeadlines() {
        taskMonitorService.checkAllTasks();
    }
}
