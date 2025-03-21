package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    private List<Resource> resources;

    public Donation(Volunteer volunteer, Resource resource) {
        this.volunteer = volunteer;
        this.resources = List.of(resource);
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
    }
}
