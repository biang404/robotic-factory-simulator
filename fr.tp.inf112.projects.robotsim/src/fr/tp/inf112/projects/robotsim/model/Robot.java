package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import java.util.List;

import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.canvas.model.impl.RGBColor;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Robot extends Component {
	
	private static final long serialVersionUID = -1218857231970296747L;

	private static final Style STYLE = new ComponentStyle(RGBColor.GREEN, RGBColor.BLACK, 3.0f, null);

	private static final Style BLOCKED_STYLE = new ComponentStyle(RGBColor.RED, RGBColor.BLACK, 3.0f, new float[]{4.0f});

	private Battery battery;
	
	private int speed;
	
	private List<Component> targetComponents;
	@JsonIgnore
	private transient Iterator<Component> targetComponentsIterator;
	@JsonIgnore
	private transient Component currTargetComponent;
	@JsonIgnore
	private transient Iterator<Position> currentPathPositionsIter;
	@JsonIgnore
	private transient boolean blocked;
	
	private Position memorizedTargetPosition;
	@JsonIgnore
	private FactoryPathFinder pathFinder;

	public Robot(final Factory factory,
				 final FactoryPathFinder pathFinder,
				 final CircularShape shape,
				 final Battery battery,
				 final String name ) {
		super(factory, shape, name);
		
		this.pathFinder = pathFinder;
		
		this.battery = battery;
		
		targetComponents = new ArrayList<>();
		currTargetComponent = null;
		currentPathPositionsIter = null;
		speed = 5;
		blocked = false;
		memorizedTargetPosition = null;
	}
	
	public Robot() {
        super(null, null, null);
        this.pathFinder = null;
        this.battery = null;
        this.targetComponents = new ArrayList<>();
        this.currTargetComponent = null;
        this.currentPathPositionsIter = null;
        this.speed = 5;
        this.blocked = false;
        this.memorizedTargetPosition = null;
    }

	@Override
	public String toString() {
		return super.toString() + " battery=" + battery + "]";
	}

	protected int getSpeed() {
		return speed;
	}

	protected void setSpeed(final int speed) {
		this.speed = speed;
	}
	
	public Position getMemorizedTargetPosition() {
		return memorizedTargetPosition;
	}
	
	private List<Component> getTargetComponents() {
		if (targetComponents == null) {
			targetComponents = new ArrayList<>();
		}
		
		return targetComponents;
	}
	
	public boolean addTargetComponent(final Component targetComponent) {
		return getTargetComponents().add(targetComponent);
	}
	
	public boolean removeTargetComponent(final Component targetComponent) {
		return getTargetComponents().remove(targetComponent);
	}
	
	@Override
	public boolean isMobile() {
		return true;
	}

	@Override
	public boolean behave() {
		if (getTargetComponents().isEmpty()) {
			return false;
		}
		
		if (currTargetComponent == null || hasReachedCurrentTarget()) {
			currTargetComponent = nextTargetComponentToVisit();
			
			computePathToCurrentTargetComponent();
		}

		return moveToNextPathPosition() != 0;
	}
		
	private Component nextTargetComponentToVisit() {
		if (targetComponentsIterator == null || !targetComponentsIterator.hasNext()) {
			targetComponentsIterator = getTargetComponents().iterator();
		}
		
		return targetComponentsIterator.hasNext() ? targetComponentsIterator.next() : null;
	}
	
	private int moveToNextPathPosition() {
	    final Motion motion = computeMotion();

	    final Position reserved = (motion != null) ? motion.getTargetPosition() : null;

	    try {
	        int displacement = (motion == null) ? 0 : getFactory().moveComponent(motion, this);

	        if (displacement != 0) {
	            blocked = false;  
	            notifyObservers();
	            return displacement;
	        }

	        if (isLivelyLocked()) {
	            final Position free = findFreeNeighbouringPosition();
	            if (free != null) {
	                boolean ok = getFactory().tryReserve(free, this);
	                try {
	                    if (ok) {
	                        Motion sidestep = new Motion(getPosition(), free);
	                        int side = getFactory().moveComponent(sidestep, this);
	                        if (side != 0) {
	                            blocked = false;
	                            computePathToCurrentTargetComponent();
	                            notifyObservers();
	                            return side;
	                        }
	                    }
	                } finally {
	                    if (ok) getFactory().releaseReserve(free, this);
	                }
	            }
	        }

	        try { Thread.sleep(50 + (int) (Math.random() * 20)); } catch (InterruptedException ignore) {}
	        return 0;

	    } finally {
	        if (reserved != null) {
	            getFactory().releaseReserve(reserved, this);
	        }
	    }
	}
	
	private void computePathToCurrentTargetComponent() {
		final List<Position> currentPathPositions = pathFinder.findPath(this, currTargetComponent);
		currentPathPositionsIter = currentPathPositions.iterator();
	}
	
	private Motion computeMotion() {
		if (currentPathPositionsIter == null) {
	        computePathToCurrentTargetComponent(); 
	    }
		
		if (!currentPathPositionsIter.hasNext()) {

			// There is no free path to the target
			blocked = true;
			
			return null;
		}
		
		
		final Position targetPosition = getTargetPosition();
		final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(),
														   targetPosition.getyCoordinate(),
				   										   2,
				   										   2);
		
		// If there is another robot, memorize the target position for the next run
		if (getFactory().hasMobileComponentAt(shape, this)) {
			this.memorizedTargetPosition = targetPosition;
			
			return null;
		}

		// Reset the memorized position
		this.memorizedTargetPosition = null;
		
		blocked = false;
		
		if (!getFactory().tryReserve(targetPosition, this)) {
		    // when it fails to reserve, memorize the target position
		    this.memorizedTargetPosition = targetPosition;
		    return null;
		}
			
		return new Motion(getPosition(), targetPosition);
	}
	
	private Position getTargetPosition() {
		// If a target position was memorized, it means that the robot was blocked during the last iteration 
		// so it waited for another robot to pass. So try to move to this memorized position otherwise move to  
		// the next position from the path
		return this.memorizedTargetPosition == null ? currentPathPositionsIter.next() : this.memorizedTargetPosition;
	}
	
	@JsonIgnore
	public boolean isLivelyLocked() {
	    if (memorizedTargetPosition == null) {
	        return false;
	    }
			
	    final Component otherComponent = getFactory().getMobileComponentAt(memorizedTargetPosition,     
	                                                                   this);

	    if (otherComponent instanceof Robot)  {
		    return getPosition().equals(((Robot) otherComponent).getMemorizedTargetPosition());
	    }
	    
	    return false;
	}

	private boolean hasReachedCurrentTarget() {
		return getPositionedShape().overlays(currTargetComponent.getPositionedShape());
	}
	
	@Override
	public boolean canBeOverlayed(final PositionedShape shape) {
		return true;
	}
	
	@Override
	public Style getStyle() {
		return blocked ? BLOCKED_STYLE : STYLE;
	}
	
	private Position findFreeNeighbouringPosition() {
	    final int step = 5; // same as Robot speed
	    int[][] dirs;

	    if (this.memorizedTargetPosition != null) {
	        final int dx = Integer.compare(
	                this.memorizedTargetPosition.getxCoordinate(), getxCoordinate());
	        final int dy = Integer.compare(
	                this.memorizedTargetPosition.getyCoordinate(), getyCoordinate());

	        if (dx != 0 && dy == 0) {
	            dirs = new int[][] {
	                    {0,  step}, {0, -step},      
	                    {-dx * step, 0}, { dx * step, 0} 
	            };
	        } else if (dy != 0 && dx == 0) {

	            dirs = new int[][] {
	                    { step, 0}, {-step, 0},   
	                    {0, -dy * step}, {0,  dy * step} 
	            };
	        } else {
	            dirs = new int[][] { {step,0}, {-step,0}, {0,step}, {0,-step} };
	        }
	    } else {
	        dirs = new int[][] { {step,0}, {-step,0}, {0,step}, {0,-step} };
	    }

	    for (int[] d : dirs) {
	        int nx = getxCoordinate() + d[0];
	        int ny = getyCoordinate() + d[1];

	        PositionedShape candidate = new RectangularShape(nx, ny, 2, 2);

	        if (!getFactory().hasObstacleAt(candidate)
	                && !getFactory().hasMobileComponentAt(candidate, this)) {
	            return new Position(nx, ny);
	        }
	    }
	    return null;
	}
}

