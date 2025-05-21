package solidarityhub.backend.observer.impl;

import solidarityhub.backend.model.Task;
import solidarityhub.backend.observer.Observable;
import org.springframework.stereotype.Component;

@Component
public class TaskObservable extends Observable {
    private Task task;

    public TaskObservable() {

    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
        setChanged();
        notifyObservers();
    }

    public void statusUpdated() {
        setChanged();
        notifyObservers();
    }
}
