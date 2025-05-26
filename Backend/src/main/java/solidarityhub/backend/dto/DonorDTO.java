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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DonorDTO donorDTO = (DonorDTO) obj;
        return dni.equals(donorDTO.dni) && name.equals(donorDTO.name);
    }

}