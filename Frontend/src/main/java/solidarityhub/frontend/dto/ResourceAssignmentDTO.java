package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class ResourceAssignmentDTO {
    private int id;
    private int taskId;
    private int resourceId;
    private String resourceName;
    private double quantity;
    private String units;

    public ResourceAssignmentDTO(int taskId, int resourceId, double quantity, String units) {
        this.taskId = taskId;
        this.resourceId = resourceId;
        this.quantity = quantity;
        this.units = units;
    }
}
