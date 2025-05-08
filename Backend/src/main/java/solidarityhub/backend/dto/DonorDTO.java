package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Donor;

@NoArgsConstructor
@Getter
@Setter
public class DonorDTO {
    private String dni;
    private String name;

    public DonorDTO(Donor donor) {
        this.dni = donor.getDni();
        this.name = donor.getName();
    }

}