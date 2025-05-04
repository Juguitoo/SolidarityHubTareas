package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import solidarityhub.backend.model.enums.ResourceType;

@Getter
@ToString
@NoArgsConstructor
@Entity
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Setter
    private double quantity;

    @Setter
    private String unit;

    @Setter
    private String cantidad;

    @Setter
    @ManyToOne
    @JoinColumn(name = "donation_id")
    private Donation donation;

    @Setter
    @ManyToOne
    @JoinColumn(name = "storage_id")
    private Storage storage;

    @Setter
    @ManyToOne
    @JoinColumn(name = "catastrophe_id")
    private Catastrophe catastrophe;


    public Resource(String name, ResourceType type, double quantity, String unit, Storage storage, Catastrophe catastrophe) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = quantity + " " + unit;
        this.storage = storage;
        this.catastrophe = catastrophe;
    }
    public Resource(String name, ResourceType type, double quantity, String unit, Catastrophe catastrophe) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = quantity + " " + unit;
        this.storage = null;
        this.catastrophe = null;
    }


}
