package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceQuantityDTO {
    private ResourceDTO resource;
    private Double availableQuantity;

    public ResourceQuantityDTO(ResourceDTO resource, Double availableQuantity) {
        this.resource = resource;
        this.availableQuantity = availableQuantity;
    }
}
