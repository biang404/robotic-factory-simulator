package fr.tp.inf112.projects.robotsim.model;

import fr.tp.inf112.projects.canvas.controller.Observer;

public interface FactoryModelChangedNotifier {

    void notifyObservers();

    boolean addObserver(Object observer);

    boolean removeObserver(Object observer);
}
