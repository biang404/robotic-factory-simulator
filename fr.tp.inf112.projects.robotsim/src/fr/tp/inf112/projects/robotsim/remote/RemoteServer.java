package fr.tp.inf112.projects.robotsim.remote;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;


public class RemoteServer {
	  public static void main(String[] args) throws IOException {
		  CanvasChooser chooser = new FileCanvasChooser("data", "");
		  FactoryPersistenceManager pm = new FactoryPersistenceManager(chooser);

	    try (ServerSocket ss = new ServerSocket(5050)) {
	      while (true) {
	        Socket s = ss.accept();
	        new Thread(new RequestProcessor(s, pm)).start();
	      }
	    }
	  }
	}
