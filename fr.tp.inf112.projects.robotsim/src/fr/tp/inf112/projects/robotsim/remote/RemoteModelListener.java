package fr.tp.inf112.projects.robotsim.remote;

import fr.tp.inf112.projects.robotsim.model.Factory;

public interface RemoteModelListener {
    void onRemoteModelLoaded(String id, Factory factory);
}