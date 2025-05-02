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
    @Column(unique = true)
    private String code; // DON-YYYY-NNN format

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
    @JoinColumn(name = "volunteer_dni")
    private Volunteer volunteer;

    @OneToMany(mappedBy = "donation")
    private List<Resource> resources;


    @Setter
    @ManyToOne
    @JoinColumn(name = "catastrophe_id")
    private Catastrophe catastrophe;

    @Setter
    private double amount;


    public Donation(String code, DonationType type, String description,
                    LocalDate date, DonationStatus status,
                    Volunteer volunteer, Catastrophe catastrophe) {
        this.code = code;
        this.type = type;
        this.description = description;
        this.date = date;
        this.status = status;
        this.volunteer = volunteer;
        this.catastrophe = catastrophe;
    }

    //Para donaciones monetarias
    public Donation(String code, DonationType type, String description,
                    LocalDate date, DonationStatus status,
                    Volunteer volunteer, Catastrophe catastrophe, double amount) {
        this(code, type, description, date, status, volunteer, catastrophe);
        this.amount = amount;
    }

}
