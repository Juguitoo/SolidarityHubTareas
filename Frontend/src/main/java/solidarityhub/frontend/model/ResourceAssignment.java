package solidarityhub.frontend.model;

import lombok.Setter;

public class ResourceAssignment {
    private int id;

    private Task task;

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
