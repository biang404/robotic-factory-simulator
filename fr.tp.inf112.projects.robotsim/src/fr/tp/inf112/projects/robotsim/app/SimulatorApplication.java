package fr.tp.inf112.projects.robotsim.app;

import java.awt.Component;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.canvas.view.CanvasViewer;
import fr.tp.inf112.projects.robotsim.model.Area;
import fr.tp.inf112.projects.robotsim.model.Battery;
import fr.tp.inf112.projects.robotsim.model.ChargingStation;
import fr.tp.inf112.projects.robotsim.model.Conveyor;
import fr.tp.inf112.projects.robotsim.model.Door;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.LocalFactoryModelChangedNotifier;
import fr.tp.inf112.projects.robotsim.model.Machine;
import fr.tp.inf112.projects.robotsim.model.Robot;
import fr.tp.inf112.projects.robotsim.model.Room;
import fr.tp.inf112.projects.robotsim.model.path.CustomDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicPolygonShape;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;
import fr.tp.inf112.projects.robotsim.remote.RemoteFileCanvasChooser;
import fr.tp.inf112.projects.robotsim.remote.RemoteFactoryPersistenceManager;
public class SimulatorApplication {
	
	private static final Logger LOGGER =
            Logger.getLogger(SimulatorApplication.class.getName());
	
	public static Factory buildFactory() {
		final Factory factory = new Factory(200, 200, "Simple Test Puck Factory");
		final Room room1 = new Room(factory, new RectangularShape(20, 20, 75, 75), "Production Room 1");
		new Door(room1, Room.WALL.BOTTOM, 10, 20, true, "Entrance");
		final Area area1 = new Area(room1, new RectangularShape(35, 35, 50, 50), "Production Area 1");
		final Machine machine1 = new Machine(area1, new RectangularShape(50, 50, 15, 15), "Machine 1");

		final Room room2 = new Room(factory, new RectangularShape( 120, 22, 75, 75 ), "Production Room 2");
		new Door(room2, Room.WALL.LEFT, 10, 20, true, "Entrance");
		final Area area2 = new Area(room2, new RectangularShape( 135, 35, 50, 50 ), "Production Area 1");
		final Machine machine2 = new Machine(area2, new RectangularShape( 150, 50, 15, 15 ), "Machine 1");
		
		final int baselineSize = 3;
		final int xCoordinate = 10;
		final int yCoordinate = 165;
		final int width =  10;
		final int height = 30;
		final BasicPolygonShape conveyorShape = new BasicPolygonShape();
		conveyorShape.addVertex(new BasicVertex(xCoordinate, yCoordinate));
		conveyorShape.addVertex(new BasicVertex(xCoordinate + width, yCoordinate));
		conveyorShape.addVertex(new BasicVertex(xCoordinate + width, yCoordinate + height - baselineSize));
		conveyorShape.addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height - baselineSize));
		conveyorShape.addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height));
		conveyorShape.addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height));
		conveyorShape.addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height - baselineSize));
		conveyorShape.addVertex(new BasicVertex(xCoordinate, yCoordinate + height - baselineSize));

		final Room chargingRoom = new Room(factory, new RectangularShape(125, 125, 50, 50), "Charging Room");
		new Door(chargingRoom, Room.WALL.RIGHT, 10, 20, false, "Entrance");
		final ChargingStation chargingStation = new ChargingStation(factory, new RectangularShape(150, 145, 15, 15), "Charging Station");

		final FactoryPathFinder jgraphPahtFinder = new JGraphTDijkstraFactoryPathFinder(factory, 5);
		final Robot robot1 = new Robot(factory, jgraphPahtFinder, new CircularShape(5, 5, 2), new Battery(10), "Robot 1");
		robot1.addTargetComponent(machine1);
		robot1.addTargetComponent(machine2);
		robot1.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));

		final FactoryPathFinder customPathFinder = new CustomDijkstraFactoryPathFinder(factory, 5);
		final Robot robot2 = new Robot(factory, customPathFinder, new CircularShape(45, 5, 2), new Battery(10), "Robot 2");
		//robot2.addTargetComponent(chargingStation);
		robot2.addTargetComponent(machine1);
		robot2.addTargetComponent(machine2);
		robot2.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));
		
		return factory;
	}
	
	public enum StartMode {
	    LOCAL,
	    REMOTE
	}
	
	public enum NotificationMode {
	    CLASSIC,    
	    NOTIFIER_LOCAL 
	}
	
	private static NotificationMode askNotificationMode() {
	    String[] options = { "Classic (Observer classique)", "Notifier (mode Kafka local)" };
	    int choice = JOptionPane.showOptionDialog(
	            null,
	            "Choisir le mode de notification :",
	            "Mode de notification",
	            JOptionPane.DEFAULT_OPTION,
	            JOptionPane.QUESTION_MESSAGE,
	            null,
	            options,
	            options[0]);

	    if (choice == 1) {
	        return NotificationMode.NOTIFIER_LOCAL;
	    }
	    return NotificationMode.CLASSIC;
	}

	
	private static StartMode askStartMode() {
	    String[] options = { "Local", "Remote" };
	    int choice = JOptionPane.showOptionDialog(
	            null,
	            "Choisir le mode de simulation :",
	            "Mode de démarrage",
	            JOptionPane.DEFAULT_OPTION,
	            JOptionPane.QUESTION_MESSAGE,
	            null,
	            options,
	            options[0]);

	    if (choice == 1) {
	        return StartMode.REMOTE;
	    }

	    return StartMode.LOCAL;
	}

	public static void main(String[] args) {
		LOGGER.info("Starting the robot simulator...");
		LOGGER.config("With parameters " + Arrays.toString(args) + ".");

		Factory factory = buildFactory();
		
		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
		        final String SERVER_HOST = "localhost";
		        final int    SERVER_PORT = 5050;

		        final RemoteFileCanvasChooser canvasChooser =
		                new RemoteFileCanvasChooser(SERVER_HOST, SERVER_PORT);

		        final RemoteFactoryPersistenceManager pm =
		                new RemoteFactoryPersistenceManager(canvasChooser, SERVER_HOST, SERVER_PORT);

		        StartMode mode = askStartMode();
		        NotificationMode notificationMode = askNotificationMode();
		        
		        if (notificationMode == NotificationMode.NOTIFIER_LOCAL) {
		            factory.setNotifier(new LocalFactoryModelChangedNotifier());
		        } else {
		            LOGGER.info("Using classic observer notification mode.");
		        }

		        Component viewer;

		        if (mode == StartMode.LOCAL) {

		            SimulatorController controller =
		                    new SimulatorController(factory, pm);

		            viewer = new CanvasViewer(controller);
		            canvasChooser.setViewer(viewer);

		        } else {
		            Factory dummyFactory = factory;

		            RemoteSimulatorController controller =
		                    new RemoteSimulatorController(
		                            dummyFactory,
		                            pm,
		                            "localhost",
		                            8080
		                    );

		            pm.setRemoteModelListener((id, remoteFactory) -> {
		                controller.setModelId(id);
		                controller.setCanvas(remoteFactory);
		            });

		            viewer = new CanvasViewer(controller);
		            canvasChooser.setViewer(viewer);
		        }
		    }
		});
	}
}
