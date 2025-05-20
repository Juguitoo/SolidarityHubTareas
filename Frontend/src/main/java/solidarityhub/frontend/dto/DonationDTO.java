package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.frontend.model.Donation;
import org.pingu.domain.enums.DonationType;
import org.pingu.domain.enums.DonationStatus;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class DonationDTO {
    private int id;
    private String code;
    private DonationType type;
    private String description;
    private LocalDate date;
    private DonationStatus status;
    private String donorDni;
    private String donorName;
    private Integer catastropheId;
    private double quantity;
    private String unit;
    private String cantidad;
    private boolean updateResources = true;

    public DonationDTO(Donation donation) {
        this.id = donation.getId();
        this.type = donation.getType();
        this.description = donation.getDescription();
        this.date = donation.getDate();
        this.status = donation.getStatus();
        this.donorDni = donation.getDonor().getDni();
        this.donorName = donation.getDonor().getName();
        this.catastropheId = donation.getCatastrophe().getId();
        this.quantity = donation.getQuantity();
        this.unit = donation.getUnit();
        this.cantidad = donation.getCantidad();
    }

    public String getCantidad() {
        if (cantidad == null || cantidad.isEmpty()) {
            return quantity + " " + unit;
        }
        return cantidad;
    }
}