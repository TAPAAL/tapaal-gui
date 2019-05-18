package dk.aau.cs.gui.components;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MovePlaceTransitionObject;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPathPoint;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.undo.DeleteArcPathPointEdit;

public class SmartDrawWorker {
	PlaceTransitionObject startingObject;
	int xSpacing;
	int ySpacing;
	DrawingSurfaceImpl drawingSurface;
	String searchOption;
	
	ArrayList<PlaceTransitionObject> objectsPlaced = new ArrayList<PlaceTransitionObject>();
	ArrayList<PlaceTransitionObject> placeTransitionObjects = new ArrayList<PlaceTransitionObject>();
	ArrayList<Point> pointsReserved = new ArrayList<Point>();
	pipe.gui.undo.UndoManager undoManager = CreateGui.getDrawingSurface().getUndoManager();
	
	//weights
	int fortyFiveDegreeWeight = 8;
	int ninetyDegreeWeight = 5;
	int distanceWeight = 10;

	
	//For BFS
	ArrayList<PlaceTransitionObject> newlyPlacedObjects = new ArrayList<PlaceTransitionObject>();

	//For DFS
	ArrayList<PlaceTransitionObject> unfinishedObjects = new ArrayList<PlaceTransitionObject>();
	ArrayList<Arc> arcsVisited = new ArrayList<Arc>();
	
	public SmartDrawWorker(int xSpacing, int ySpacing, DrawingSurfaceImpl drawingSurface, String searchOption) {
		this.startingObject = findStartingObjectCandidate();
		this.xSpacing = xSpacing;
		this.ySpacing = ySpacing;
		this.drawingSurface = drawingSurface;
		this.searchOption = searchOption;
		getPlaceTransitionObjects(); 
	}
	
	public void smartDraw() {
		undoManager.newEdit();
		//We need a better way to choose the first object
		PlaceTransitionObject startingObject = findStartingObjectCandidate();
		
		//We place the first object at hard coordinates
		Point startingPoint = new Point(500,350);
		Command command = new MovePlaceTransitionObject(startingObject, startingPoint);
		command.redo();
		undoManager.addEdit(command);
		pointsReserved.add(startingPoint);
		
		if(searchOption == "DFS") {
			objectsPlaced = new ArrayList<PlaceTransitionObject>();
			objectsPlaced.add(startingObject);
			unfinishedObjects = new ArrayList<PlaceTransitionObject>();
			unfinishedObjects.add(startingObject);
			arcsVisited = new ArrayList<Arc>();
			while(!(unfinishedObjects.isEmpty())) {
				PlaceTransitionObject nextObject = unfinishedObjects.get(unfinishedObjects.size()-1);
				depthFirstDraw(nextObject);
			}
		} else {
			objectsPlaced = new ArrayList<PlaceTransitionObject>();
			objectsPlaced.add(startingObject);
			newlyPlacedObjects.add(startingObject);
			
			while(!(newlyPlacedObjects.isEmpty())) {
				PlaceTransitionObject newParent = newlyPlacedObjects.get(0);
				breadthFirstDraw(newParent);
			}
		}
		moveObjectsWithinOrigo();
		
		
		removeArcPathPoints();
		
		CreateGui.getDrawingSurface().repaintAll();
		CreateGui.getModel().repaintAll(true);
		CreateGui.getDrawingSurface().updatePreferredSize();
	}
	
	private void depthFirstDraw(PlaceTransitionObject parentObject) {
		Iterator<Arc> arcFromIterator = parentObject.getConnectFromIterator();
		Command command;
		boolean objectPlaced = false;
		boolean objectHasUnvisitedArcs = false;
		outerloop: while(arcFromIterator.hasNext()) {
			Arc arcTraversed = arcFromIterator.next();
			if(!(arcsVisited.contains(arcTraversed))) {
				PlaceTransitionObject objectToPlace = arcTraversed.getTarget();
				if(!(objectsPlaced.contains(objectToPlace))) {
					objectPlaced = false;
					/* layer defines what layer we are on 
					 * in the grid like structure.
					 * Imagine circles within circles
					 */
					int layer = 0;
					while(!objectPlaced) {
						layer += 1;
						//Try different positions for the objects
						for(int x = ((int)parentObject.getPositionX() - (xSpacing*layer)); x <= ((int)parentObject.getPositionX() + (xSpacing*layer)); x += xSpacing) {
							for(int y = ((int)parentObject.getPositionY() - (ySpacing * layer)); y <= ((int)parentObject.getPositionY() + (ySpacing*layer)); y += ySpacing) {
								Point possiblePoint = new Point(x, y);
								if(!(pointsReserved.contains(possiblePoint))) {
									command = new MovePlaceTransitionObject(objectToPlace, possiblePoint);
									command.redo();
									undoManager.addEdit(command);
									
									//Reserve the point and let the object in the queue
									pointsReserved.add(possiblePoint);
									unfinishedObjects.add(objectToPlace);
									//Don't visit the same arc twice or place the same object twice
									arcsVisited.add(arcTraversed);
									objectsPlaced.add(objectToPlace);
									objectPlaced = true;
									break outerloop;
								}
							}
						}
					}
				}	
			}
		}
		//These remove the object if we have visited all arcs from it
		while(arcFromIterator.hasNext()) {
			if(!(arcsVisited.contains(arcFromIterator.next()))) {
				objectHasUnvisitedArcs = true;
			}
		}
		if(objectHasUnvisitedArcs == false) {
			unfinishedObjects.remove(parentObject);
		}
	}
	
	private void breadthFirstDraw(PlaceTransitionObject parentObject) {
		Iterator<Arc> arcFromIterator = parentObject.getConnectFromIterator();
		Command command;
		boolean objectPlaced = false;
		while(arcFromIterator.hasNext()) {
			PlaceTransitionObject objectToPlace = arcFromIterator.next().getTarget();
			//Check if we already placed it to avoid infinite loops
			if(!(objectsPlaced.contains(objectToPlace))) {
				objectPlaced = false;
				/* layer defines what layer we are on 
				 * in the grid like structure.
				 * Imagine circles within circles
				 */
				int layer = 0;
				while(!objectPlaced) {
					layer += 1;
					//Try different positions for the objects
					outerloop: for(int x = ((int)parentObject.getPositionX() - (xSpacing*layer)); x <= ((int)parentObject.getPositionX() + (xSpacing*layer)); x += xSpacing) {
						for(int y = ((int)parentObject.getPositionY() - (ySpacing * layer)); y <= ((int)parentObject.getPositionY() + (ySpacing*layer)); y += ySpacing) {
							Point possiblePoint = new Point(x, y);
							if(!(pointsReserved.contains(possiblePoint))) {
								command = new MovePlaceTransitionObject(objectToPlace, possiblePoint);
								command.redo();
								undoManager.addEdit(command);
								//Reserve the point and let the object in the queue
								pointsReserved.add(possiblePoint);
								newlyPlacedObjects.add(objectToPlace);
								// Don't place the same object twice
								objectsPlaced.add(objectToPlace);
								objectPlaced = true;
								break outerloop;
							}
						}
					}
				}
			}
		}
		newlyPlacedObjects.remove(parentObject);
	}
	
	private PlaceTransitionObject findStartingObjectCandidate() {
		//We first try to find an object with no incoming arcs
		//and atleast 1 outgoing arc as our starting point
		PlaceTransitionObject candidate = findObjectWithZeroToArcs();
		Iterator<Arc> arcFromIterator;
		Iterator<Arc> arcToIterator;

		int numberOfToArcs = 0;
		int numberOfFromArcs = 0;
		int candidateDifference = 0;
		if(candidate == null) {
			for(PlaceTransitionObject ptObject : placeTransitionObjects) {
				arcFromIterator = ptObject.getConnectFromIterator();
				arcToIterator = ptObject.getConnectToIterator();
				while(arcToIterator.hasNext()) {
					arcToIterator.next();
					numberOfToArcs++;
				}
				while(arcFromIterator.hasNext()) {
					arcFromIterator.next();
					numberOfFromArcs++;
				}
				int difference = numberOfFromArcs - numberOfToArcs;
				if(numberOfToArcs > candidateDifference) {
					candidateDifference = difference;
					candidate = ptObject;
				}
			}
		}
		return candidate;
	}
	
	private PlaceTransitionObject findObjectWithZeroToArcs() {
		PlaceTransitionObject candidate = null;
		Iterator<Arc> arcFromIterator;
		int numberOfFromArcs = 0;
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			arcFromIterator = ptObject.getConnectFromIterator();
			while(arcFromIterator.hasNext()) {
				arcFromIterator.next();
				numberOfFromArcs++;
			}
			if(ptObject.getConnectToIterator().hasNext() == false && numberOfFromArcs >= 1) {
				candidate = ptObject;
				break;
			}
		}
		return candidate;
	}
	
	/*
	 * Find better name
	 * We move all the objects so that their y-value and x-value >= 20
	 * Else it creates bugs with the scrollbar
	 * So we push all objects by some factor on the y and x axis
	 */
	private void moveObjectsWithinOrigo() {
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
			Command command;
			for(PlaceTransitionObject ptObject : placeTransitionObjects) {
				int newX = (int) (ptObject.getPositionX() + Math.abs(lowestX) + 50);
				Point newPosition = new Point(newX, (int) ptObject.getPositionY());
				command = new MovePlaceTransitionObject(ptObject, newPosition);
				command.redo();
				undoManager.addEdit(command);
				
			}
		}
		if(lowestY < 50) {
			Command command;
			for(PlaceTransitionObject ptObject : placeTransitionObjects) {
				int newY = (int) (ptObject.getPositionY() + Math.abs(lowestY) + 50);
				Point newPosition = new Point((int) ptObject.getPositionX(), newY);
				command = new MovePlaceTransitionObject(ptObject, newPosition);
				command.redo();
				undoManager.addEdit(command);
			}
		}
	}
	
	private void removeArcPathPoints() {
		ArrayList<ArcPathPoint> toRemove = new ArrayList<ArcPathPoint>();
		for(PetriNetObject arc : CreateGui.getDrawingSurface().getPNObjects()) {
			if(arc instanceof Arc) {
				ArrayList<ArcPathPoint> arcPathPoints =(ArrayList<ArcPathPoint>) ((Arc) arc).getArcPath().getArcPathPoints();
				for(ArcPathPoint arcPathPoint : arcPathPoints) {
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
	
	private void getPlaceTransitionObjects() {
		placeTransitionObjects = new ArrayList<PlaceTransitionObject>();
		for(PetriNetObject object : drawingSurface.getPlaceTransitionObjects()) {
			if(object instanceof PlaceTransitionObject) {
				PlaceTransitionObject ptObject = (PlaceTransitionObject) object;
				placeTransitionObjects.add(ptObject);
			}
		}
	}
	//For debugging
	private void printPTObjectsAndPositions() {
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			System.out.println("Name: " + ptObject.getName() + " X: " + ptObject.getPositionX() + " Y: " + ptObject.getPositionY());
		}
	}
}
