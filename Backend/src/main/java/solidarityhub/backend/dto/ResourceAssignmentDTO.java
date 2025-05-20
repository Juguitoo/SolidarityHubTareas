package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.ResourceAssignment;

@NoArgsConstructor
@Getter
@Setter
public class ResourceAssignmentDTO {
    private int id;
    private int taskId;
    private int resourceId;
    private String resourceName;
    private double quantity;
    private String units;

    public ResourceAssignmentDTO(ResourceAssignment assignment) {
        this.id = assignment.getId();
        this.taskId = assignment.getTask().getId();
        this.resourceId = assignment.getResource().getId();
        this.resourceName = assignment.getResource().getName();
        this.quantity = assignment.getQuantity();
        this.units = assignment.getUnits();
    }
}
