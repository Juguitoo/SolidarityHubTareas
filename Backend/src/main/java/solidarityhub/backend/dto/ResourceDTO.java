package solidarityhub.backend.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.model.enums.ResourceType;

@NoArgsConstructor
@Getter
@Setter
public class ResourceDTO {
    private int id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private double quantity;

    private String unit;

    private Integer storageId;

    private Integer catastropheId;

    public ResourceDTO(Resource resource) {
        this.id = resource.getId();
        this.name = resource.getName();
        this.type = resource.getType();
        this.quantity = resource.getQuantity();
        this.unit = resource.getUnit();
        this.storageId = (resource.getStorage() != null) ? resource.getStorage().getId() : null;
        if(resource.getCatastrophe() != null) {
            this.catastropheId = resource.getCatastrophe().getId();
        } else {
            this.catastropheId = null;
        }
    }

    public String getCantidad() {
        return quantity + " " + unit;
    }

}
