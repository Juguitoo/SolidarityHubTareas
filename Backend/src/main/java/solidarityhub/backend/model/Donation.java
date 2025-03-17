package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @Setter
    @JoinColumn(name = "volunteer_dni")
    private Volunteer volunteer;

    @OneToMany(mappedBy = "donation")
    private Set<Resource> resources;

    public Donation(Volunteer volunteer, Resource resource) {
        this.volunteer = volunteer;
        this.resources = Set.of(resource);
    }
}
