package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.model.enums.DonationStatus;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class DonationDTO {
    private int id;
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

    public DonationDTO(Donation donation) {
        this.id = donation.getId();
        this.type = donation.getType();
        this.description = donation.getDescription();
        this.date = donation.getDate();
        this.status = donation.getStatus();

        if (donation.getDonor() != null) {
            this.donorDni = donation.getDonor().getDni();
            this.donorName = donation.getDonor().getName();
        }

        if (donation.getCatastrophe() != null) {
            this.catastropheId = donation.getCatastrophe().getId();
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DonationDTO)) return false;

        DonationDTO that = (DonationDTO) o;

        if (id != that.id) return false;
        if (Double.compare(that.quantity, quantity) != 0) return false;
        if (!type.equals(that.type)) return false;
        if (!description.equals(that.description)) return false;
        if (!date.equals(that.date)) return false;
        if (status != that.status) return false;
        if (!donorDni.equals(that.donorDni)) return false;
        if (!donorName.equals(that.donorName)) return false;
        if (catastropheId != null ? !catastropheId.equals(that.catastropheId) : that.catastropheId != null)
            return false;
        return unit.equals(that.unit);
    }
}