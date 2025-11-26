package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.List;

import fr.tp.inf112.projects.canvas.controller.Observer;

public class LocalFactoryModelChangedNotifier implements FactoryModelChangedNotifier {

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void notifyObservers() {
    	System.out.println("[LocalNotifier] notifyObservers() called!");
        for (Observer o : observers) { System.out.println("[LocalNotifier] Notifying observer: " + o);
            o.modelChanged();
        }
    }

    @Override
    public boolean addObserver(Object observer) {if (observer instanceof Observer o) {
        System.out.println("[LocalNotifier] addObserver(): " + o);
        return observers.add(o);
    }
    return false;
    }

    @Override
    public boolean removeObserver(Object observer) {
        if (observer instanceof Observer o) {
            System.out.println("[LocalNotifier] removeObserver(): " + o);
            return observers.remove(o);
        }
        return false;
    }
}
