package solidarityhub.backend.observer.impl;

import solidarityhub.backend.model.Storage;
import solidarityhub.backend.observer.Observable;

public class StorageObservable extends Observable {
    private Storage storage;

    public StorageObservable() {
        this.storage = null;
    }

    public StorageObservable(Storage storage) {
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
        setChanged();
        notifyObservers();
    }

    public void resourceAdded(Storage storage) {
        this.storage = storage;
        setChanged();
        notifyObservers();
    }
}
