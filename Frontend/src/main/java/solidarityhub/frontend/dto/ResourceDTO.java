package solidarityhub.frontend.dto;


import lombok.Getter;
import lombok.Setter;
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
    private Storage storage;
    private Integer catastropheId;

    public ResourceDTO(String name, String type, double quantity, Storage storage) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.storage = storage;
        this.catastropheId = null;
    }

    public ResourceDTO(int id, String name, String type, double quantity, Storage storage, Integer catastropheId) {
        this(name, type, quantity, storage);
        this.catastropheId = catastropheId;
    }

}
