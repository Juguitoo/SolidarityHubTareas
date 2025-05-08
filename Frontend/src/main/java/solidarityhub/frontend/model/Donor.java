package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Donor {
    private String dni;
    private String name;
    private List<Donation> donations = new ArrayList<>();

    public Donor(String dni, String firstName, String name) {
        this.dni = dni;
        this.name = firstName;
    }
}