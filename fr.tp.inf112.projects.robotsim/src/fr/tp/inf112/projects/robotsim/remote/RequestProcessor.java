package fr.tp.inf112.projects.robotsim.remote;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

public class RequestProcessor implements Runnable {
    private final Socket socket;
    private final FactoryPersistenceManager manager;

    public RequestProcessor(Socket socket, FactoryPersistenceManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    @Override public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream  ois = new ObjectInputStream(socket.getInputStream());
            try {
                Object req = ois.readObject();

                if (req instanceof String id) {
                    if (Proto.LIST.equals(id)) {
                        File dir = new File("data"); // save directory
                        String[] names = dir.list((d, name) -> new File(d, name).isFile());
                        if (names == null) names = new String[0];
                        oos.writeObject(names);     // return list of saved models
                    } else {
                        // read model
                        Canvas canvas = manager.read(id);
                        oos.writeObject(canvas);    // write back the model
                    }

                } else if (req instanceof Factory f) {
                    // save model
                    manager.persist(f);
                    oos.writeObject(Boolean.TRUE);

                } else {
                    oos.writeObject(new IllegalArgumentException("Unsupported type: " + req));
                }

                oos.flush();
            } catch (Exception ex) {
                // return the exception to the client
                oos.writeObject(ex);
                oos.flush();
            } finally {
                try { ois.close(); } catch (Exception ignore) {}
                try { oos.close(); } catch (Exception ignore) {}
            }
        } catch (Exception outer) {
            outer.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception ignore) {}
        }
    }
}
