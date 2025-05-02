package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.model.enums.DonationStatus;
import java.time.LocalDate;


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
    private Volunteer volunteer;

    @Setter
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
    public Donation(String code, DonationType type, String description,
                    LocalDate date, DonationStatus status,
                    Volunteer volunteer, Catastrophe catastrophe, double amount) {
        this(code, type, description, date, status, volunteer, catastrophe);
        this.amount = amount;
    }
}
