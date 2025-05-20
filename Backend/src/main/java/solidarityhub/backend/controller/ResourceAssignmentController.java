package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.ResourceAssignmentDTO;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.ResourceAssignment;
import solidarityhub.backend.service.ResourceAssignmentService;
import solidarityhub.backend.service.ResourceService;
import solidarityhub.backend.service.TaskService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/resource-assignments")
public class ResourceAssignmentController {
    private final ResourceAssignmentService assignmentService;
    private final ResourceService resourceService;
    private final TaskService taskService;

    public ResourceAssignmentController(
            ResourceAssignmentService assignmentService,
            ResourceService resourceService,
            TaskService taskService) {
        this.assignmentService = assignmentService;
        this.resourceService = resourceService;
        this.taskService = taskService;
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getAssignmentsByTask(@PathVariable int taskId) {
        List<ResourceAssignmentDTO> assignmentDTOs = new ArrayList<>();
        assignmentService.getAssignmentsByTask(taskId).forEach(assignment -> {
            assignmentDTOs.add(new ResourceAssignmentDTO(assignment));
        });
        return ResponseEntity.ok(assignmentDTOs);
    }

    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<?> getAssignmentsByResource(@PathVariable int resourceId) {
        List<ResourceAssignmentDTO> assignmentDTOs = new ArrayList<>();
        assignmentService.getAssignmentsByResource(resourceId).forEach(assignment -> {
            assignmentDTOs.add(new ResourceAssignmentDTO(assignment));
        });
        return ResponseEntity.ok(assignmentDTOs);
    }

    @GetMapping("/available/{resourceId}")
    public ResponseEntity<?> getAvailableQuantity(@PathVariable int resourceId) {
        Double available = assignmentService.getAvailableQuantity(resourceId);
        return ResponseEntity.ok(available);
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(@RequestBody ResourceAssignmentDTO assignmentDTO) {
        ResourceAssignment assignment = assignmentService.assignResourceToTask(
                assignmentDTO.getResourceId(),
                assignmentDTO.getTaskId(),
                assignmentDTO.getQuantity(),
                assignmentDTO.getUnits()
        );

        if (assignment == null) {
            return ResponseEntity.badRequest().body("No se pudo asignar el recurso a la tarea. " +
                    "Verifica que existan tanto el recurso como la tarea y que haya suficiente cantidad disponible.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResourceAssignmentDTO(assignment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable int id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/assigned-total/{resourceId}")
    public ResponseEntity<?> getTotalAssignedQuantity(@PathVariable int resourceId) {
        Double totalAssigned = assignmentService.getTotalAssignedQuantity(resourceId);

        if (totalAssigned == null) {
            totalAssigned = 0.0;
        }

        return ResponseEntity.ok(totalAssigned);
    }
}
