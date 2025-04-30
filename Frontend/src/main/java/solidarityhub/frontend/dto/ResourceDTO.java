package solidarityhub.frontend.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.Storage;

@NoArgsConstructor
@Getter
@Setter
public class ResourceDTO {
    private int id;
    private String name;
    private String type;
    private double quantity;
    private String unit;
    private String cantidad;
    private Storage storage;
    private Integer catastropheId;

    public ResourceDTO(String name, String type, double quantity, String unit, Storage storage) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = String.valueOf(quantity) + " " + unit;
        this.storage = storage;
        this.catastropheId = null;
    }

    public ResourceDTO(int id, String name, String type, double quantity, String unit, Storage storage, Integer catastropheId) {
        this(name, type, quantity, unit, storage);
        this.catastropheId = catastropheId;
    }

}
