package fr.tp.slr201.projects.robotsim.service.simulation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import fr.tp.inf112.projects.robotsim.model.Factory;

public class RemoteFactoryPersistenceManagerNoCanvas {

    private final String host;
    private final int port;

    public RemoteFactoryPersistenceManagerNoCanvas(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Factory readFactory(String canvasId) throws IOException, ClassNotFoundException {
        if (canvasId == null || canvasId.isBlank()) {
            throw new IllegalArgumentException("canvasId must not be null/blank");
        }
        String toSend = ensureServerDataPath(canvasId);

        try (Socket s = new Socket(host, port)) {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

            // 请求：发送字符串 id
            oos.writeObject(toSend);
            oos.flush();

            Object resp = ois.readObject();

            if (resp instanceof Exception e) {
                throw new IOException("Server error on read(" + canvasId + ")", e);
            }
            if (!(resp instanceof Factory)) {
                throw new IOException("Unexpected type from server for id=" + canvasId
                        + ": " + (resp == null ? "null" : resp.getClass().getName()));
            }
            return (Factory) resp;
        }
    }

    private static String ensureServerDataPath(String id) {
        String t = id.trim().replace('\\','/');
        if (t.isEmpty()) return t;
        if (t.startsWith("data/") || t.contains("/")) return t;
        return "data/" + t;
    }
}