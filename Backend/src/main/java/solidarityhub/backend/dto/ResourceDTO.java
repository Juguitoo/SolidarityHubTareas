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

    private int quantity;

    private Storage storage;

    private Integer catastropheId;

    public ResourceDTO(Resource resource) {
        this.id = resource.getId();
        this.name = resource.getName();
        this.type = resource.getType();
        this.quantity = (int) resource.getQuantity();
        this.storage = resource.getStorage();
        this.catastropheId = resource.getCatastrophe().getId();
    }


}
