package solidarityhub.backend.observer;

import java.util.ArrayList;
import java.util.List;

// Observable abstract class
public abstract class Observable {
    private List<Observer> observers = new ArrayList<>();
    private boolean changed = false;

    public void addObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(Object arg) {
        List<Observer> localObservers;

        synchronized (this) {
            if (!changed) {
                return;
            }
            localObservers = new ArrayList<>(observers);
            changed = false;
        }

        for (Observer observer : localObservers) {
            observer.update(this, arg);
        }
    }

    protected void setChanged() {
        changed = true;
    }

    protected void clearChanged() {
        changed = false;
    }

    public boolean hasChanged() {
        return changed;
    }

    public int countObservers() {
        return observers.size();
    }
}
