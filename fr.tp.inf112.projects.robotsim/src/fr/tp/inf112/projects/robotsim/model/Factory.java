package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import fr.tp.inf112.projects.canvas.controller.Observable;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Factory extends Component implements Canvas, Observable {

	private static final long serialVersionUID = 5156526483612458192L;
	
	private static final ComponentStyle DEFAULT = new ComponentStyle(5.0f);

	@JsonManagedReference
    private final List<Component> components;
    @JsonIgnore
	private transient List<Observer> observers;
    
	private boolean simulationStarted;
	@JsonIgnore
	private final java.util.Map<Position, Component> reservations = new java.util.HashMap<>();
	private FactoryModelChangedNotifier notifier;

	public synchronized boolean tryReserve(Position pos, Component who) {
	    if (pos == null) return false;
	    Component holder = reservations.get(pos);
	    if (holder == null || holder == who) {
	        reservations.put(pos, who);
	        return true;
	    }
	    return false;
	}
	
	public synchronized void releaseReserve(Position pos, Component who) {
	    Component holder = reservations.get(pos);
	    if (holder == who) {
	        reservations.remove(pos);
	    }
	}
	
	public Factory(final int width,
				   final int height,
				   final String name ) {
		super(null, new RectangularShape(0, 0, width, height), name);
		
		components = new ArrayList<>();
		observers = null;
		simulationStarted = false;
	}
	
	public Factory() {
		super(null, null, null);
		components = new ArrayList<>();
		observers = null;
		simulationStarted = false;
	}
	
	public List<Observer> getObservers() {
		if (observers == null) {
			observers = new ArrayList<>();
		}
		
		return observers;
	}

	@Override
	public boolean addObserver(Observer observer) {
	    if (notifier != null) {
	        return notifier.addObserver(observer);
	    }
	    return getObservers().add(observer);
	}

	@Override
	public boolean removeObserver(Observer observer) {
	    if (notifier != null) {
	        return notifier.removeObserver(observer);
	    }
	    return getObservers().remove(observer);
	}

	public void notifyObservers() {
	    if (notifier != null) {
	        notifier.notifyObservers();
	    } else {
	        for (final Observer observer : getObservers()) {
	            observer.modelChanged();
	        }
	    }
	}

	public boolean addComponent(final Component component) {
		if (components.add(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	public boolean removeComponent(final Component component) {
		if (components.remove(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	protected List<Component> getComponents() {
		return components;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@JsonIgnore
	@Override
	public Collection<Figure> getFigures() {
		return (Collection) components;
	}

	@Override
	public String toString() {
		return super.toString() + " components=" + components + "]";
	}
	
	public boolean isSimulationStarted() {
		return simulationStarted;
	}

	public void startSimulation() {
		if (!isSimulationStarted()) {
			this.simulationStarted = true;
			notifyObservers();

			behave();
		}
	}

	public void stopSimulation() {
		if (isSimulationStarted()) {
			this.simulationStarted = false;
			
			notifyObservers();
		}
	}

	@Override
	public boolean behave() {
		boolean started = false;
		
		for (final Component component : getComponents()) {
	        Thread t = new Thread(component, component.getClass().getSimpleName() + "-thread");
	        t.setDaemon(true);
	        t.start();
	        started = true;
	    }

	    return started;
	}
	
	@Override
	public Style getStyle() {
		return DEFAULT;
	}
	
	public boolean hasObstacleAt(final PositionedShape shape) {
		for (final Component component : getComponents()) {
			if (component.overlays(shape) && !component.canBeOverlayed(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasMobileComponentAt(final PositionedShape shape,
										final Component movingComponent) {
		for (final Component component : getComponents()) {
			if (component != movingComponent && component.isMobile() && component.overlays(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Component getMobileComponentAt(	final Position position,
											final Component ignoredComponent) {
		if (position == null) {
			return null;
		}
		
		return getMobileComponentAt(new RectangularShape(position.getxCoordinate(), position.getyCoordinate(), 2, 2), ignoredComponent);
	}
	
	public Component getMobileComponentAt(	final PositionedShape shape,
											final Component ignoredComponent) {
		if (shape == null) {
			return null;
		}
		
		for (final Component component : getComponents()) {
			if (component != ignoredComponent && component.isMobile() && component.overlays(shape)) {
				return component;
			}
		}
		
		return null;
	}
	
	public synchronized int moveComponent(final Motion motion, final Component componentToMove) {
	    if (motion == null) {
	        return 0;
	    }

	    // detect obstacles at target position
	    PositionedShape targetShape = new RectangularShape(
	            motion.getTargetPosition().getxCoordinate(),
	            motion.getTargetPosition().getyCoordinate(),
	            2, 2);

	    if (hasObstacleAt(targetShape) || hasMobileComponentAt(targetShape, componentToMove)) {
	        return 0;
	    }

	    // execute movement
	    int displacement = motion.moveToTarget();

	    if (displacement != 0) {
	        notifyObservers();
	    }

	    return displacement;
	}
	@JsonIgnore
	public java.util.List<Component> getAllComponents() {
        return components;
    }
	
	public void refreshCanvas() {
        notifyObservers();
    }
	
	public void setNotifier(FactoryModelChangedNotifier notifier) {
	    this.notifier = notifier;
	}

	public FactoryModelChangedNotifier getNotifier() {
	    return notifier;
	}
	
	
}
