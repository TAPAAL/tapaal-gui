package dk.aau.cs.gui.smartDraw;

import java.awt.Point;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.SwingWorker;

import dk.aau.cs.gui.undo.UpdateNameLabelOffsetCommand;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MovePlaceTransitionObject;
import dk.aau.cs.util.Require;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPathPoint;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.graphicElements.Transition;
import pipe.gui.undo.DeleteArcPathPointEdit;
import pipe.gui.undo.TransitionRotationEdit;

public class SmartDrawWorker extends SwingWorker<Void, Void>{
	List<SmartDrawListener> listeners = new ArrayList<SmartDrawListener>();
	PlaceTransitionObject startingObject;
	int xSpacing;
	int ySpacing;
	DrawingSurfaceImpl drawingSurface;
	String searchOption;
	Point rootPoint;
	Point rightMostPointUsed = new Point(0, 0);
	Boolean isDone = false;
	
	ArrayList<PlaceTransitionObject> objectsPlaced = new ArrayList<PlaceTransitionObject>();
	ArrayList<PlaceTransitionObject> placeTransitionObjects = new ArrayList<PlaceTransitionObject>();
	ArrayList<Point> pointsReserved = new ArrayList<Point>();
	pipe.gui.undo.UndoManager undoManager = CreateGui.getDrawingSurface().getUndoManager();
	
	//weights
	int diagonalWeight;
	int straightWeight;
	int distanceWeight;
	int overlappingArcWeight;
	int minimumIterations;

	
	//For BFS
	ArrayList<PlaceTransitionObject> newlyPlacedObjects = new ArrayList<PlaceTransitionObject>();

	//For DFS
	ArrayList<PlaceTransitionObject> unfinishedObjects = new ArrayList<PlaceTransitionObject>();
	ArrayList<Arc> arcsVisited = new ArrayList<Arc>();
	
	public SmartDrawWorker(int xSpacing, int ySpacing, DrawingSurfaceImpl drawingSurface, String searchOption, 
			int straightWeight, int diagonalWeight, int distanceWeight, int overlappingArcWeight, String startingObject, int minimumIterations) {
		this.xSpacing = xSpacing;
		this.ySpacing = ySpacing;
		this.drawingSurface = drawingSurface;
		this.searchOption = searchOption;
		this.straightWeight = straightWeight;
		this.diagonalWeight = diagonalWeight;
		this.distanceWeight = distanceWeight;
		this.overlappingArcWeight = overlappingArcWeight;
		this.minimumIterations = minimumIterations;
		
			
		getPlaceTransitionObjects(); 
		processStartingObject(startingObject);
	}
	
	private void processStartingObject(String startingObject) {
		if(!(startingObject.equals("Random")))
			this.startingObject = drawingSurface.getPlaceTransitionObjectByName(startingObject);
		else {
			try {
				this.startingObject = placeTransitionObjects.get(new Random().nextInt(placeTransitionObjects.size()-1));
			} catch (IllegalArgumentException e) {
				this.startingObject = placeTransitionObjects.get(0);
			}
		}
	}
	
	@Override
	public Void doInBackground() throws Exception {
		undoManager.newEdit();		
		
		
		arcsVisited = new ArrayList<Arc>();
		objectsPlaced = new ArrayList<PlaceTransitionObject>();
		
		fireStartDraw();
		
		

		//Do for unconnected nets too
		while(!(objectsPlaced.containsAll(placeTransitionObjects))) {
			if(objectsPlaced.isEmpty()) {
				//We place the first object at hard coordinates 
				rootPoint = new Point(500,350);
			} else {
				for(PlaceTransitionObject object : placeTransitionObjects) {
					if(!(objectsPlaced.contains(object))) {
						startingObject = object;
						break;
					}
				}
				rootPoint = new Point(rightMostPointUsed.x + 500, 350);
			}
			moveObject(startingObject, rootPoint);
			rightMostPointUsed = rootPoint;
			reservePoint(rootPoint);
			objectsPlaced.add(startingObject);

			if(searchOption == "DFS") {
				unfinishedObjects = new ArrayList<PlaceTransitionObject>();
				unfinishedObjects.add(startingObject);
					while(!(unfinishedObjects.isEmpty())) {
						PlaceTransitionObject nextObject = unfinishedObjects.get(unfinishedObjects.size()-1);
						depthFirstDraw(nextObject);
					}
			} else {
				newlyPlacedObjects.add(startingObject);
				
				while(!(newlyPlacedObjects.isEmpty())) {
					PlaceTransitionObject newParent = newlyPlacedObjects.get(0);
					breadthFirstDraw(newParent);
				}
			}
		}
		moveObjectsWithinScreenEdge();
		removeArcPathPoints();
		resetLabelsToDefault();
		
		
		
		
		
		return null;
	}
	
	private void depthFirstDraw(PlaceTransitionObject parentObject) {
		ArrayList<Arc> arcsForObject = getAllArcsFromObject(parentObject);
		PlaceTransitionObject objectToPlace;
		boolean objectPlaced = false;
		Point parentPoint = new Point((int)parentObject.getPositionX(), (int)parentObject.getPositionY());
		
		outerloop: for(Arc arc : arcsForObject) {
			if(!(arcsVisited.contains(arc))) {
				arcsVisited.add(arc);
				if(arc.getTarget() != parentObject) {objectToPlace = arc.getTarget();} 
				else {objectToPlace = arc.getSource();}
				
				if(!(objectsPlaced.contains(objectToPlace))) {

					objectPlaced = false;
					int smallestWeight = Integer.MAX_VALUE;
					Point bestPoint = null;
					/* layer defines what layer we are on 
					 * in the grid like structure.
					 * Imagine circles within circles
					 */
					int layer = 0;
					while(!objectPlaced) {
						layer += 1;
						//Try different positions for the objects
						for(int x = (parentPoint.x - (xSpacing*layer)); x <= (parentPoint.x + (xSpacing*layer)); x += xSpacing) {
							for(int y = (parentPoint.y - (ySpacing * layer)); y <= (parentPoint.y + (ySpacing*layer)); y += ySpacing) {
								Point possiblePoint = new Point(x, y);
								if(!(pointsReserved.contains(possiblePoint))) {

									int weight = calculateWeight(possiblePoint, layer, getObjectPositionAsPoint(parentObject), objectToPlace);

									if(weight < smallestWeight) {
										smallestWeight = weight;
										bestPoint = possiblePoint;
									}
								}
							}
						}
						//We try at least minimumIterations times
						if(layer >= minimumIterations && bestPoint != null) {
							fireStatusChanged(objectsPlaced.size());
							moveObject(objectToPlace, bestPoint);
							checkIfObjectIsNowRightmost(bestPoint);
							//Reserve the point and let the object in the queue
							reservePoint(bestPoint);
							unfinishedObjects.add(objectToPlace);
							//Don't visit the same arc twice or place the same object twice
							objectsPlaced.add(objectToPlace);
							objectPlaced = true;
							break outerloop;
						}
					}
				}	
			}
		}
		//These remove the object if we have visited all arcs from it
		if(arcsVisited.containsAll(arcsForObject)) {
			unfinishedObjects.remove(parentObject);
		}
	}
	
	private ArrayList<Arc> getAllArcsFromObject(PlaceTransitionObject object) {
		ArrayList<Arc> arcsForObject = new ArrayList<Arc>();
		Iterator<Arc> fromIterator = object.getConnectFromIterator();
		Iterator<Arc> toIterator = object.getConnectToIterator();
		Arc arc;
		while(fromIterator.hasNext()) {
			arc = fromIterator.next();
			arcsForObject.add(arc);
		}
		while(toIterator.hasNext()) {
			arc = toIterator.next();
			arcsForObject.add(arc);
		}
		return arcsForObject;
	}
	private void moveObject(PlaceTransitionObject object, Point point) {
		Command command = new MovePlaceTransitionObject(object, point);
		undoManager.addEdit(command);
		command.redo();
	}
	private void reservePoint(Point point) {
		pointsReserved.add(point);
	}
	
	private void breadthFirstDraw(PlaceTransitionObject parentObject) {
		ArrayList<Arc> arcsForObject = getAllArcsFromObject(parentObject);
		PlaceTransitionObject objectToPlace;
		boolean objectPlaced = false;
		Point parentPoint = new Point((int)parentObject.getPositionX(), (int)parentObject.getPositionY());
		for(Arc arc : arcsForObject) {
			if(arc.getTarget() != parentObject) {objectToPlace = arc.getTarget();} 
			else {objectToPlace = arc.getSource();}
			arcsVisited.add(arc);
			//Check if we already placed it to avoid infinite loops
			if(!(objectsPlaced.contains(objectToPlace))) {
				objectPlaced = false;
				int smallestWeight = Integer.MAX_VALUE;
				Point bestPoint = null;
				/* layer defines what layer we are on 
				 * in the grid like structure.
				 * Imagine circles within circles
				 */
				int layer = 0;
				while(!objectPlaced) {
					layer += 1;
					//Try different positions for the objects
					for(int x = (parentPoint.x - (xSpacing*layer)); x <= (parentPoint.x + (xSpacing*layer)); x += xSpacing) {
						for(int y = (parentPoint.y - (ySpacing * layer)); y <= (parentPoint.y + (ySpacing*layer)); y += ySpacing) {
							Point possiblePoint = new Point(x, y);
							int weight = calculateWeight(possiblePoint, layer, getObjectPositionAsPoint(parentObject), objectToPlace);

							if(weight < smallestWeight) {
								smallestWeight = weight;
								bestPoint = possiblePoint;
							}
						}
					}
					//We try at least minimumiterations times
					if(!(pointsReserved.contains(bestPoint)) && layer >= minimumIterations) {
						fireStatusChanged(objectsPlaced.size());
						moveObject(objectToPlace, bestPoint);
						checkIfObjectIsNowRightmost(bestPoint);
						//Reserve the point and let the object in the queue
						reservePoint(bestPoint);
						newlyPlacedObjects.add(objectToPlace);
						// Don't place the same object twice
						objectsPlaced.add(objectToPlace);
						objectPlaced = true;
					}
				}
			}
		}
		newlyPlacedObjects.remove(parentObject);
	}
	private int calculateWeight(Point candidatePoint, int layer, Point parentPoint, PlaceTransitionObject objectToPlace) {
		int weight = 0;
		if(candidatePoint.x == parentPoint.x || candidatePoint.y == parentPoint.y) {
			weight += straightWeight * layer;
		} else {
			weight += diagonalWeight * layer;
		}
		weight += distanceWeight * ((Math.abs(candidatePoint.x - rootPoint.x) + Math.abs(candidatePoint.y - rootPoint.y)) / 1000);
		weight += calculateNumberOfOverlappingArcs(candidatePoint, objectToPlace) * overlappingArcWeight;
		return weight;
	}
	
	private int calculateNumberOfOverlappingArcs(Point candidatePoint, PlaceTransitionObject objectToPlace) {
		int number = 0;
		Point source;
		Point target;
		double distanceTargetSource;
		double distancePointSource;
		double distanceTargetPoint;
		for(PlaceTransitionObject object : objectsPlaced) {
			for(Arc placedArc : getAllArcsFromObject(object)) {
				source = getObjectPositionAsPoint(placedArc.getSource());
				target = getObjectPositionAsPoint(placedArc.getTarget());
				
				distanceTargetSource = Point.distance(source.x, source.y, target.x, target.y);
				distanceTargetPoint = Point.distance(candidatePoint.x, candidatePoint.y, target.x, target.y);
				distancePointSource = Point.distance(candidatePoint.x, candidatePoint.y, source.x, source.y);
				
				//T --- newObject --->S
				if((distancePointSource + distanceTargetPoint) == distanceTargetSource)
					number += 1;
				
				//newObject --- S ---> T
				else if((distancePointSource + distanceTargetSource) == distanceTargetPoint) {
					for(PetriNetObject pNetObject : drawingSurface.getPNObjects()) {
						if(pNetObject instanceof Arc) {
							Arc arc = (Arc)pNetObject;
							if((arc.getSource() == objectToPlace && arc.getTarget() == placedArc.getTarget()) 
									|| (arc.getTarget() == objectToPlace && arc.getSource() == placedArc.getTarget())) {
								number += 1;
							}
						}
					}
				}
				
				//newObject --- T ---> S
				else if((distanceTargetSource + distanceTargetPoint == distancePointSource)) {
					for(PetriNetObject pNetObject : drawingSurface.getPNObjects()) {
						if(pNetObject instanceof Arc) {
							Arc arc = (Arc)pNetObject;
							if((arc.getSource() == objectToPlace && arc.getTarget() == placedArc.getSource()) 
									|| (arc.getTarget() == objectToPlace && arc.getSource() == placedArc.getSource())) {
								number += 1;
							}
						}
					}
				}
						
			}
		}
		
		return number;
	}
	
	public Point getObjectPositionAsPoint(PlaceTransitionObject object) {
		return new Point((int) object.getPositionX(), (int)object.getPositionY());
	}
	
	private void checkIfObjectIsNowRightmost(Point newPoint) {
		if(newPoint.x > rightMostPointUsed.x) {
			rightMostPointUsed = newPoint;
		}
	}

	
	/*
	 * Find better name
	 * We move all the objects so that their y-value and x-value >= 20
	 * Else it creates bugs with the scrollbar
	 * So we push all objects by some factor on the y and x axis
	 */
	private void moveObjectsWithinScreenEdge() {
		int lowestY = 50;
		int lowestX = 50;
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			if(ptObject.getPositionX() < lowestX) {
				lowestX = (int) ptObject.getPositionX();
			}
			if(ptObject.getPositionY() < lowestY) {
				lowestY = (int) ptObject.getPositionY();
			}
		}
		if(lowestX < 50) {
			for(PlaceTransitionObject ptObject : placeTransitionObjects) {
				int newX = (int) (ptObject.getPositionX() + Math.abs(lowestX) + 50);
				Point newPosition = new Point(newX, (int) ptObject.getPositionY());
				moveObject(ptObject, newPosition);
				
			}
		}
		if(lowestY < 50) {
			for(PlaceTransitionObject ptObject : placeTransitionObjects) {
				int newY = (int) (ptObject.getPositionY() + Math.abs(lowestY) + 50);
				Point newPosition = new Point((int) ptObject.getPositionX(), newY);
				moveObject(ptObject, newPosition);
			}
		}
	}
	
	private void removeArcPathPoints() {
		ArrayList<ArcPathPoint> toRemove = new ArrayList<ArcPathPoint>();
		for(PetriNetObject object : CreateGui.getDrawingSurface().getPNObjects()) {
			if(object instanceof ArcPathPoint) {
				ArcPathPoint arcPathPoint = (ArcPathPoint)object;
				if(!(arcPathPoint.isEndPoint())) {
					toRemove.add(arcPathPoint);
				}
			}

		}
		for(ArcPathPoint p : toRemove) {
			Command command = new DeleteArcPathPointEdit(p.getArcPath().getArc(), p, p.getIndex());
			command.redo();
			undoManager.addEdit(command);
		}
	}
	
	public void setTransitionsToUpright() {
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			if(ptObject instanceof Transition) {
				Transition transition = (Transition)ptObject;
				int newAngle = 0 - transition.getAngle();
				Command command = new TransitionRotationEdit(transition, newAngle);
				command.redo();
				undoManager.addEdit(command);
			}
		}
	}
	
	public void doOffsetForLoops() {
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			if(ptObject instanceof Place) {
				Place place = (Place)ptObject;
				for(Arc arcOne : place.getPostset()) {
					Transition transition = (Transition)arcOne.getTarget();
					for(Arc arcTwo : transition.getPostset()){
						if(arcTwo.getTarget() == place) {
							offsetArcPointsFromMiddlepoint(arcOne, arcTwo, place, transition);
						}
					}
				}
			}
		}
	}
	
	/*
	 * Add arcPathPoints for arcs where
	 * A---> B --> A so they do not overlap
	 * could maybe be a for loop or something instead
	 */
	private void offsetArcPointsFromMiddlepoint(Arc arcOne, Arc arcTwo, Place place, Transition transition) {
		Point.Float pointForArcOne = new Point.Float();
		Point.Float pointForArcTwo = new Point.Float();
		if(transition.getPositionX() == place.getPositionX()) {
			pointForArcOne = new Point.Float(arcOne.getArcPath().midPoint.x+30, (arcOne.getArcPath().midPoint.y));
			pointForArcTwo = new Point.Float(arcTwo.getArcPath().midPoint.x-30, (arcTwo.getArcPath().midPoint.y));
		} 
		else if(transition.getPositionY() == place.getPositionY()) {
			pointForArcOne = new Point.Float((arcOne.getArcPath().midPoint.x), arcOne.getArcPath().midPoint.y+30);
			pointForArcTwo = new Point.Float((arcTwo.getArcPath().midPoint.x), arcTwo.getArcPath().midPoint.y-30);
		} else {
			pointForArcOne = new Point.Float((arcOne.getArcPath().midPoint.x+15), arcOne.getArcPath().midPoint.y+15);
			pointForArcTwo = new Point.Float((arcTwo.getArcPath().midPoint.x-15), arcTwo.getArcPath().midPoint.y-15);
		}
		
		undoManager.addEdit(arcOne.getArcPath().insertPoint(pointForArcOne, false));
		undoManager.addEdit(arcTwo.getArcPath().insertPoint(pointForArcTwo, false));

	}
	
	private void getPlaceTransitionObjects() {
		placeTransitionObjects = new ArrayList<PlaceTransitionObject>();
		for(PetriNetObject object : drawingSurface.getPlaceTransitionObjects()) {
			if(object instanceof PlaceTransitionObject) {
				PlaceTransitionObject ptObject = (PlaceTransitionObject) object;
				placeTransitionObjects.add(ptObject);
			}
		}
	}
	public void resetLabelsToDefault() {
		for(PetriNetObject pNetObject : drawingSurface.getPNObjects()) {
			if(pNetObject instanceof PlaceTransitionObject) {
				Command cmd = new UpdateNameLabelOffsetCommand(pipe.gui.Pipe.DEFAULT_OFFSET_X, pipe.gui.Pipe.DEFAULT_OFFSET_Y, ((PlaceTransitionObject) pNetObject).getNameOffsetX(), 
																((PlaceTransitionObject) pNetObject).getNameOffsetY(), pNetObject);
				cmd.redo();
				undoManager.addEdit(cmd);
				
			}
			else if(pNetObject instanceof Arc) {
				Command cmd = new UpdateNameLabelOffsetCommand(0, 0, ((Arc) pNetObject).getNameOffsetX(), 
						((Arc) pNetObject).getNameOffsetY(), pNetObject);
				cmd.redo();
				undoManager.addEdit(cmd);
			}
		}
	}
	
	
	public void addSmartDrawListener(SmartDrawListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.add(listener);
	}

	public void removeSmartDrawListener(SmartDrawListener listener){
		Require.that(listener != null, "Listener cannot be null");
		listeners.remove(listener);
	}
	
	void fireStatusChanged(int objectsPlaced) {
		for(SmartDrawListener listener : listeners) {
			listener.fireStatusChanged(objectsPlaced);
		}
	}
	void fireStartDraw() {
		for(SmartDrawListener listener : listeners) {
			listener.fireStartDraw();
		}
	}
	void fireDone(boolean cancelled){
		for(SmartDrawListener listener : listeners) {
			listener.fireDone(cancelled);
		}
	}
	
	@Override
	protected void done(){
		if(objectsPlaced.size() == drawingSurface.getPlaceTransitionObjects().size()) {
			setTransitionsToUpright();
			doOffsetForLoops();
			CreateGui.getModel().repaintAll(true);
			CreateGui.getDrawingSurface().updatePreferredSize();
			fireDone(false);
		} else {
			fireDone(true);
		}

		
	}
	//For debugging
	private void printPTObjectsAndPositions() {
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			System.out.println("Name: " + ptObject.getName() + " X: " + ptObject.getPositionX() + " Y: " + ptObject.getPositionY());
		}
	}
}
