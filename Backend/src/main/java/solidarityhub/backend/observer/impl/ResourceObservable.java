package solidarityhub.backend.observer.impl;

import java.util.List;

import solidarityhub.backend.model.Resource;
import solidarityhub.backend.observer.Observable;

public class ResourceObservable extends Observable {
    private List<Resource> resources;

    public ResourceObservable(List<Resource> resources) {
        this.resources = resources;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
        setChanged();
        notifyObservers();
    }

    public void resourceUpdated(Resource resource) {
        // Notify observers when a single resource is updated
        setChanged();
        notifyObservers(resource);
    }
}
