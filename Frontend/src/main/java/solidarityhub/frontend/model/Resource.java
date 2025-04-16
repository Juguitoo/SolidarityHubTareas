package solidarityhub.frontend.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.enums.ResourceType;

@Getter
@NoArgsConstructor
public class Resource {

    private int id;

    @Setter
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Setter
    private double quantity;

    @Setter
    private Donation donation;

    @Setter
    private Storage storage;

    public Resource(ResourceType type, double quantity) {
        this.type = type;
        this.quantity = quantity;
    }
}
