package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pingu.domain.enums.ResourceType;

@NoArgsConstructor
@Getter
@Setter
public class ResourceSummaryDTO {
    private ResourceType type;
    private int count;
    private double totalQuantity;
    private double assignedQuantity;
    private double availableQuantity;

    public ResourceSummaryDTO(ResourceType type, int count, double totalQuantity,
                              double assignedQuantity) {
        this.type = type;
        this.count = count;
        this.totalQuantity = totalQuantity;
        this.assignedQuantity = assignedQuantity;
        this.availableQuantity = totalQuantity - assignedQuantity;
    }

    public double getUsagePercentage() {
        if (totalQuantity == 0) {
            return 0;
        }
        return (assignedQuantity / totalQuantity) * 100;
    }
}
