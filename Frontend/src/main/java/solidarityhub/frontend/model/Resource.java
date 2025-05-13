package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pingu.domain.enums.ResourceType;

@Getter
@NoArgsConstructor
public class Resource {
    private int id;

    @Setter
    private String name;

    @Setter
    private ResourceType type;

    @Setter
    private double quantity;

    @Setter
    private String unit;

    @Setter
    private String cantidad;

    @Setter
    private Donation donation;

    @Setter
    private Storage storage;

    public Resource(String name, ResourceType type, double quantity, String unit, Storage storage) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = String.valueOf(quantity) + " " + unit;
        this.storage = storage;
    }
}
