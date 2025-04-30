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
    private ResourceType type;

    @Setter
    private double quantity;

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


    public Resource(String name, ResourceType type, double quantity, Storage storage, Catastrophe catastrophe) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.storage = storage;
        this.catastrophe = catastrophe;
    }
    public Resource(String name, ResourceType type, double quantity, Catastrophe catastrophe) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.storage = null;
        this.catastrophe = null;
    }


}
