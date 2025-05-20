package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
public class ResourceAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Setter
    private double quantity;

    @Setter
    private String units;

    public ResourceAssignment(Task task, Resource resource, double quantity, String units) {
        this.task = task;
        this.resource = resource;
        this.quantity = quantity;
        this.units = units;
    }
}
