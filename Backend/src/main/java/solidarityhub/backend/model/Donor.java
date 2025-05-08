package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Donor {
    @Id
    private String dni;

    @Column(nullable = false)
    private String Name;

    @OneToMany(mappedBy = "donor")
    private List<Donation> donations;

    public Donor(String dni, String name) {
        this.donations = new ArrayList<>();
        this.dni = dni;
        this.Name = name;
    }
}