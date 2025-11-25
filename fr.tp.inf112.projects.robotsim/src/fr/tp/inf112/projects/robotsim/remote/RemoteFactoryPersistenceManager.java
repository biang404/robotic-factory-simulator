package fr.tp.inf112.projects.robotsim.remote;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RemoteFactoryPersistenceManager implements CanvasPersistenceManager {

    private final CanvasChooser chooser;
    private final String host;
    private final int port;
    
    private RemoteModelListener listener;

    public RemoteFactoryPersistenceManager(CanvasChooser chooser, String host, int port) {
        if (chooser == null) throw new IllegalArgumentException("chooser must not be null");
        this.chooser = chooser;
        this.host = host;
        this.port = port;
    }
    
    public void setRemoteModelListener(RemoteModelListener listener) {
        this.listener = listener;
    }
    
    private static String ensureServerDataPath(String id) {
        if (id == null) return null;
        String t = id.trim().replace('\\','/');
        if (t.isEmpty()) return t;
        if (t.startsWith("data/") || t.contains("/")) return t;
        return "data/" + t;
    }

    @Override
    public Canvas read(String canvasId) throws IOException {
        if (canvasId == null || canvasId.isBlank()) {
            throw new IllegalArgumentException("canvasId must not be null/blank");
        }
        String toSend = ensureServerDataPath(canvasId);
        try (Socket s = new Socket(host, port)) {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.flush();
            ObjectInputStream  ois = new ObjectInputStream(s.getInputStream());

            oos.writeObject(toSend);
            oos.flush();

            Object resp = ois.readObject();
            if (resp instanceof Exception e) {
                throw new IOException("Server error on read(" + canvasId + ")", e);
            }

            Canvas c = (Canvas) resp;

            if (listener != null && c instanceof Factory) {
                String id = ensureServerDataPath(canvasId);
                listener.onRemoteModelLoaded(id, (Factory) c);
            }

            return c;

        } catch (ClassCastException cce) {
            throw new IOException("Server returned unexpected type for id=" + canvasId, cce);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception ex) {
            throw new IOException("Remote read failed for id=" + canvasId, ex);
        }
    }

    @Override
    public void persist(Canvas canvasModel) throws IOException {
        if (!(canvasModel instanceof Factory)) {
            throw new IOException("Remote persist expects a Factory (Canvas is " +
                    (canvasModel == null ? "null" : canvasModel.getClass().getName()) + ')');
        }
        Factory f = (Factory) canvasModel;

        String id = null;
        if (this.chooser != null) {
            try {
                id = this.chooser.newCanvasId(); 
            } catch (IOException e) {
                throw new IOException("Failed to get new canvas id from chooser", e);
            }
        }
        if (id == null || id.isBlank()) {
            throw new IOException("No file name provided for saving.");
        }
        id = ensureServerDataPath(id);

        try {
            var setId = f.getClass().getMethod("setId", String.class);
            setId.invoke(f, id);
        } catch (NoSuchMethodException e) {
            System.err.println("Warning: Factory has no setId(String). Make sure FactoryPersistenceManager uses the id properly.");
        } catch (Exception e) {
            throw new IOException("Cannot set Factory id", e);
        }

        try (Socket s = new Socket(host, port)) {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

            oos.writeObject(f);
            oos.flush();

            Object resp = ois.readObject();
            if (resp instanceof Exception e) {
                throw new IOException("Server error on persist(" + id + ")", e);
            }
            if (resp instanceof Boolean b && Boolean.TRUE.equals(b)) {
                return;
            }
            throw new IOException("Unexpected server reply on persist: " + resp);

        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception ex) {
            throw new IOException("Remote persist failed", ex);
        }
    }


    @Override
    public boolean delete(Canvas canvasModel) throws IOException {
        return false;
    }

    @Override
    public CanvasChooser getCanvasChooser() {
        return chooser;
    }
}

