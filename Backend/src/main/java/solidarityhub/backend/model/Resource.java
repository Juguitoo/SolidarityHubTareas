package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private String type;

    private double quantity;

    @Setter
    @ManyToOne
    @JoinColumn(name = "donation_id")
    private Donation donation;

    @Setter
    @ManyToOne
    @JoinColumn(name = "storage_id")
    private Storage storage;

    public Resource(String type,double quantity) {
        this.type = type;
        this.quantity = quantity;
    }

}
