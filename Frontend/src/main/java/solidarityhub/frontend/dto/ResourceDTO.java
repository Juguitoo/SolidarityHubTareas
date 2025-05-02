package solidarityhub.frontend.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.Storage;
import solidarityhub.frontend.model.enums.ResourceType;

@NoArgsConstructor
@Getter
@Setter
public class ResourceDTO {
    private int id;
    private String name;
    private ResourceType type;
    private double quantity;
    private String unit;
    private String cantidad;
    private Integer storageId;
    private Integer catastropheId;

    public ResourceDTO(String name, ResourceType type, double quantity, String unit, Integer storageId) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = String.valueOf(quantity) + " " + unit;
        this.storageId = storageId;
        this.catastropheId = null;
    }

    public ResourceDTO(String name, ResourceType type, double quantity, String unit, Integer storageId, Integer catastropheId) {
        this(name, type, quantity, unit, storageId);
        this.catastropheId = catastropheId;
    }

}
