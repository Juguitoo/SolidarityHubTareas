package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
public class Donation {
    private int id;

    @Setter
    private Volunteer volunteer;

    @Setter
    private List<Resource> resources;

    public Donation(Volunteer volunteer, Resource resource) {
        this.volunteer = volunteer;
        this.resources = List.of(resource);
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
    }
}
