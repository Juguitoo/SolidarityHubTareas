package solidarityhub.backend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private ResourceType type;

    private int quantity;

    private Storage storage;

    public ResourceDTO(Resource resource) {
        this.id = resource.getId();
        this.name = resource.getName();
        this.type = resource.getType();
        this.quantity = (int) resource.getQuantity();
        this.storage = resource.getStorage();
    }


}
