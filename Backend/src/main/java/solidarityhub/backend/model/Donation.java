package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.model.enums.DonationStatus;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    @Enumerated(EnumType.STRING)
    private DonationType type;

    @Setter
    private String description;

    @Setter
    private LocalDate date;

    @Setter
    @Enumerated(EnumType.STRING)
    private DonationStatus status;

    @ManyToOne
    @Setter
    @JoinColumn(name = "donor_dni")
    private Donor donor;

    @OneToMany(mappedBy = "donation")
    private List<Resource> resources;

    @Setter
    @ManyToOne
    @JoinColumn(name = "catastrophe_id")
    private Catastrophe catastrophe;

    @Setter
    private double quantity;

    @Setter
    private String unit;

    @Setter
    private String cantidad;


    public Donation(DonationType type, String description,
                    LocalDate date, DonationStatus status,
                    Donor donor, Catastrophe catastrophe) {
        this.type = type;
        this.description = description;
        this.date = date;
        this.status = status;
        this.donor = donor;
        this.catastrophe = catastrophe;
    }

    public Donation(DonationType type, String description,
                    LocalDate date, DonationStatus status,
                    Donor donor, Catastrophe catastrophe,
                    double quantity, String unit) {
        this(type, description, date, status, donor, catastrophe);
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = quantity + " " + unit;
    }
}