package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Affected extends Person {
    @Setter
    private String affectedAddress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private GPSCoordinates gpsCoordinates;

    @Setter
    @Column(nullable = false)
    private boolean disability;

    @OneToMany(mappedBy = "affected")
    private List<Need> needs;

    public Affected(String dNI, String firstName, String lastName, String email, int phone,
                    String address, String password, boolean disability) {
        super(dNI, firstName, lastName, email, phone, address, password);
        this.disability = disability;
        this.needs = new ArrayList<>();
    }

    public void addNeed(Need need) {
        this.needs.add(need);
    }

}

