package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.ResourceAssignment;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.repository.ResourceAssignmentRepository;
import solidarityhub.backend.repository.ResourceRepository;
import solidarityhub.backend.repository.TaskRepository;

import java.util.List;
import java.util.Map;

@Service
public class ResourceAssignmentService {
    private final ResourceAssignmentRepository assignmentRepository;
    private final ResourceRepository resourceRepository;
    private final TaskRepository taskRepository;

    public ResourceAssignmentService(ResourceAssignmentRepository assignmentRepository,
                                     ResourceRepository resourceRepository,
                                     TaskRepository taskRepository) {
        this.assignmentRepository = assignmentRepository;
        this.resourceRepository = resourceRepository;
        this.taskRepository = taskRepository;
    }

    public ResourceAssignment assignResourceToTask(int resourceId, int taskId, double quantity, String units) {
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        Task task = taskRepository.findById(taskId).orElse(null);

        if (resource == null || task == null) {
            return null;
        }

        // Check if there's enough quantity available
        Double totalAssigned = assignmentRepository.getTotalAssignedQuantity(resourceId);
        if (totalAssigned == null) {
            totalAssigned = 0.0;
        }

        if (resource.getQuantity() - totalAssigned < quantity) {
            return null; // Not enough resources available
        }

        ResourceAssignment assignment = new ResourceAssignment(task, resource, quantity, units);
        resource.addAssignment(assignment);
        task.addResourceAssignment(assignment);

        return assignmentRepository.save(assignment);
    }

    public List<ResourceAssignment> getAssignmentsByTask(int taskId) {
        return assignmentRepository.findByTaskId(taskId);
    }

    public List<ResourceAssignment> getAssignmentsByResource(int resourceId) {
        return assignmentRepository.findByResourceId(resourceId);
    }

    public Double getAvailableQuantity(int resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) {
            return 0.0;
        }

        Double assigned = assignmentRepository.getTotalAssignedQuantity(resourceId);
        if (assigned == null) {
            return resource.getQuantity();
        }

        return resource.getQuantity() - assigned;
    }

    public Double getTotalAssignedQuantity(int resourceId) {
        return assignmentRepository.getTotalAssignedQuantity(resourceId);
    }

    public void deleteAssignment(int assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    public List<Map<String, Object>> getAssignedResourcesSummary(Integer catastropheId) {
        List<Map<String, Object>> summary = assignmentRepository.getAssignedResourcesSummary(catastropheId);
        return summary;
    }
}
