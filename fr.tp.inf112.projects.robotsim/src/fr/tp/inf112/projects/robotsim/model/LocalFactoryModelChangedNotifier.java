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
    public boolean addObserver(Observer observer) {System.out.println("[LocalNotifier] addObserver(): " + observer);
        return observers.add(observer);
    }

    @Override
    public boolean removeObserver(Observer observer) {System.out.println("[LocalNotifier] removeObserver(): " + observer);
        return observers.remove(observer);
    }
}
