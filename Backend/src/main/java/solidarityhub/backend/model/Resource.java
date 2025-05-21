package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import solidarityhub.backend.model.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;



@Getter
@ToString
@NoArgsConstructor
@Entity
public class Resource {

    public static final double MINIMUM_RESOURCE_THRESHOLD = 10.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Setter
    private double quantity;

    @Setter
    private String unit;

    @Setter
    private String cantidad;

    @Setter
    @ManyToOne
    @JoinColumn(name = "donation_id")
    private Donation donation;

    @Setter
    @ManyToOne
    @JoinColumn(name = "storage_id")
    private Storage storage;

    @Setter
    @ManyToOne
    @JoinColumn(name = "catastrophe_id")
    private Catastrophe catastrophe;

    @Setter
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private List<ResourceAssignment> resourceAssignments;

    public Resource(String name, ResourceType type, double quantity, String unit, Storage storage, Catastrophe catastrophe) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = quantity + " " + unit;
        this.storage = storage;
        this.catastrophe = catastrophe;
        this.resourceAssignments = new ArrayList<>();

        // If storage is provided, add this resource to the storage
        if (storage != null) {
            storage.addResource(this);
        }
    }
    public Resource(String name, ResourceType type, double quantity, String unit, Catastrophe catastrophe) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cantidad = quantity + " " + unit;
        this.storage = null;
        this.catastrophe = catastrophe;
        this.resourceAssignments = new ArrayList<>();
    }

    public void addAssignment(ResourceAssignment assignment) {
        if (this.resourceAssignments == null) {
            this.resourceAssignments = new ArrayList<>();
        }
        this.resourceAssignments.add(assignment);
    }

    // Check if resource is below the minimum threshold
    public boolean isBelowThreshold() {
        return this.quantity < MINIMUM_RESOURCE_THRESHOLD;
    }

    // Update quantity and check for thresholds
    public boolean updateQuantity(double newQuantity) {
        boolean wasBelowThreshold = isBelowThreshold();
        this.quantity = newQuantity;
        this.cantidad = this.quantity + " " + this.unit;
        boolean isNowBelowThreshold = isBelowThreshold();

        // Return true if there was a threshold crossing
        return wasBelowThreshold != isNowBelowThreshold;
    }

    // Get available quantity (after assignments)
    public double getAvailableQuantity() {
        if (resourceAssignments == null || resourceAssignments.isEmpty()) {
            return quantity;
        }

        double assigned = resourceAssignments.stream()
                .mapToDouble(ResourceAssignment::getQuantity)
                .sum();

        return Math.max(0, quantity - assigned);
    }

}
