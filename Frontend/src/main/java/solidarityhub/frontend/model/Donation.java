package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Person;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.model.enums.DonationStatus;
import java.time.LocalDate;
import java.util.ArrayList;


@Getter
@NoArgsConstructor
public class Donation {
    private int id;

    @Setter
    private String code;

    @Setter
    private DonationType type;

    @Setter
    private String description;

    @Setter
    private LocalDate date;

    @Setter
    private DonationStatus status;

    @Setter
    private Donor donor;

    @Setter
    private Catastrophe catastrophe;

    @Setter
    private double quantity;

    @Setter
    private String unit;

    @Setter
    private String cantidad;


    public Donation(String code, DonationType type, String description,
                    LocalDate date, DonationStatus status,
                    Donor donor, Catastrophe catastrophe) {
        this.code = code;
        this.type = type;
        this.description = description;
        this.date = date;
        this.status = status;
        this.donor = donor;
        this.catastrophe = catastrophe;
    }

    public Donation(String code, DonationType type, String description,
                    LocalDate date, DonationStatus status,
                    Donor donor, Catastrophe catastrophe,
                    double quantity, String unit) {
        this(code, type, description, date, status, donor, catastrophe);
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = quantity + " " + unit;
    }
}
