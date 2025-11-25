package fr.tp.inf112.projects.robotsim.remote;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

import java.io.IOException;

public class RemoteFileCanvasChooser extends FileCanvasChooser {
  private final String host;
  private final int port;

  public RemoteFileCanvasChooser(String host, int port) {
    super("data", "rfactory");
    this.host = host;
    this.port = port;
  }

  @Override
  public String newCanvasId() throws IOException {
    String name = JOptionPane.showInputDialog(
        null,
        "Enter file name (without path):",
        "Save Remote Model",
        JOptionPane.PLAIN_MESSAGE
    );
    if (name == null) return null;
    name = name.trim();
    if (name.isEmpty()) return null;

    name = name.replace('\\', '/');
    if (name.contains("/")) name = name.substring(name.lastIndexOf('/') + 1);

    if (!name.toLowerCase().endsWith(".rfactory")) {
      name = name + ".rfactory";
    }
    return name; 
  }

  @Override
  public String choseCanvas() throws IOException {
    try (Socket s = new Socket(host, port)) {
      ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      oos.flush();
      ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

      oos.writeObject(Proto.LIST);
      oos.flush();

      Object resp = ois.readObject();
      if (resp instanceof Exception e) throw new IOException("Server error while listing", e);
      if (!(resp instanceof String[])) throw new IOException("Unexpected list response: " + resp);

      String[] names = (String[]) resp;
      if (names.length == 0) {
        JOptionPane.showMessageDialog(null, "No models found on server.", "Open", JOptionPane.INFORMATION_MESSAGE);
        return null;
      }

      Object selected = JOptionPane.showInputDialog(
          null,
          "Choose a model on " + host + ":" + port,
          "Open",
          JOptionPane.QUESTION_MESSAGE,
          null,
          names,
          names[0]
      );
      if (selected == null) return null;

      String name = selected.toString().trim();
      if (name.isEmpty()) return null;

      return name;
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IOException("Failed to list remote models", e);
    }
  }

  @Override
  public String browseCanvases(boolean open) {
    try {
      return open ? choseCanvas() : newCanvasId();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
