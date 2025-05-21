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
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @Setter
    private String name;

    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GPSCoordinates gpsCoordinates;

    @Column(nullable = false)
    @Setter
    private boolean isFull;


    // Add capacity attribute to represent maximum capacity
    @Setter
    @Column(nullable = false)
    private int capacity = 100; // Default capacity is 100 units

    // Add current usage field to track how much is being used
    @Setter
    @Column(nullable = false)
    private int currentUsage = 0;


    @Setter
    @OneToMany(mappedBy = "storage")
    private List<Resource> resources;

    @Setter
    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    public Storage(String name, GPSCoordinates gpsCoordinates, boolean isFull, Zone zone) {
        this.resources = new ArrayList<>();
        this.name = name;
        this.gpsCoordinates = gpsCoordinates;
        this.isFull = isFull;
        this.zone = zone;
        this.capacity = 100; // Default capacity
        this.currentUsage = 0;
    }

    public Storage(String name, GPSCoordinates gpsCoordinates, boolean isFull, Zone zone, int capacity) {
        this(name, gpsCoordinates, isFull, zone);
        this.capacity = capacity;
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
        // Increase the current usage
        this.currentUsage++;
        // Check if we're reaching capacity
        if (this.currentUsage >= (this.capacity * 0.9)) { // 90% full
            this.isFull = true;
        }
    }

    public void removeResource(Resource resource) {
        if (this.resources.remove(resource)) {
            // Decrease the current usage
            this.currentUsage = Math.max(0, this.currentUsage - 1);
            // Update isFull flag
            this.isFull = this.currentUsage >= (this.capacity * 0.9);
        }
    }

    // Calculate percentage of capacity used
    public double getCapacityUsagePercentage() {
        return (double) this.currentUsage / this.capacity * 100.0;
    }
}
