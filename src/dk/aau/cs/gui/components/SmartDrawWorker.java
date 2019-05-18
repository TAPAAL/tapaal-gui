package dk.aau.cs.gui.components;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;


import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MovePlaceTransitionObject;
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
	Point rootPoint;
	int i = 0;
	
	ArrayList<PlaceTransitionObject> objectsPlaced = new ArrayList<PlaceTransitionObject>();
	ArrayList<PlaceTransitionObject> placeTransitionObjects = new ArrayList<PlaceTransitionObject>();
	ArrayList<Point> pointsReserved = new ArrayList<Point>();
	pipe.gui.undo.UndoManager undoManager = CreateGui.getDrawingSurface().getUndoManager();
	
	//weights
	int diagonalWeight = 8;
	int nonDiagonalWeight = 5;
	int distanceWeight = 10;
	int overlappingArcWeight = 100;

	
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
		rootPoint = new Point(500,350);
		Command command = new MovePlaceTransitionObject(startingObject, rootPoint);
		command.redo();
		undoManager.addEdit(command);
		pointsReserved.add(rootPoint);
		
		removeArcPathPoints();
		arcsVisited = new ArrayList<Arc>();
		
		if(searchOption == "DFS") {
			objectsPlaced = new ArrayList<PlaceTransitionObject>();
			objectsPlaced.add(startingObject);
			unfinishedObjects = new ArrayList<PlaceTransitionObject>();
			unfinishedObjects.add(startingObject);
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
		
		
		
		
		CreateGui.getDrawingSurface().repaintAll();
		CreateGui.getModel().repaintAll(true);
		CreateGui.getDrawingSurface().updatePreferredSize();
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

									int weight = calculateWeight(possiblePoint, layer, parentObject, objectToPlace);

									if(weight < smallestWeight) {
										smallestWeight = weight;
										bestPoint = possiblePoint;
									}
								}
							}
						}
						//We try at least 3 times
						if(layer >= 3 && bestPoint != null) {
							moveObject(objectToPlace, bestPoint);
							
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
		command.redo();
		undoManager.addEdit(command);
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
							int weight = calculateWeight(possiblePoint, layer, parentObject, objectToPlace);

							if(weight < smallestWeight) {
								smallestWeight = weight;
								bestPoint = possiblePoint;
							}
						}
					}
					if(!(pointsReserved.contains(bestPoint)) && layer >=3) {
						moveObject(objectToPlace, bestPoint);
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
	private int calculateWeight(Point candidatePoint, int layer, PlaceTransitionObject parentObject, PlaceTransitionObject objectToPlace) {
		int weight = 0;
		if(candidatePoint.x == rootPoint.x || candidatePoint.y == rootPoint.y) {
			weight += nonDiagonalWeight * layer;
		} else {
			weight += diagonalWeight * layer;
		}
		weight += distanceWeight * ((Math.abs(candidatePoint.x - rootPoint.x) + Math.abs(candidatePoint.y - rootPoint.x)) / 1000);
		weight += calculateNumberOfOverlappingArcs(candidatePoint, parentObject, objectToPlace) * overlappingArcWeight;
		return weight;
	}
	
	private int calculateNumberOfOverlappingArcs(Point candidatePoint, PlaceTransitionObject parentObject, PlaceTransitionObject objectToPlace) {
		int number = 0;
		Point source;
		Point target;
		double distanceTargetSource;
		double distancePointSource;
		double distanceTargetPoint;
		System.out.println(arcsVisited.size());
		for(PlaceTransitionObject object : objectsPlaced) {
			for(Arc arc : getAllArcsFromObject(object)) {
				source = getObjectPositionAsPoint(arc.getSource());
				target = getObjectPositionAsPoint(arc.getTarget());
				
				distanceTargetSource = Point.distance(source.x, source.y, target.x, target.y);
				distanceTargetPoint = Point.distance(candidatePoint.x, candidatePoint.y, target.x, target.y);
				distancePointSource = Point.distance(candidatePoint.x, candidatePoint.y, source.x, source.y);
				
				if((distancePointSource + distanceTargetPoint) == distanceTargetSource)
					number += 1;
				else if((distancePointSource + distanceTargetSource) == distanceTargetPoint)
					number += 1;
				else if((distanceTargetSource + distanceTargetPoint == distancePointSource))
					number += 1;
			}
		}
		
		return number;
	}
	
	public Point getObjectPositionAsPoint(PlaceTransitionObject object) {
		return new Point((int) object.getPositionX(), (int)object.getPositionY());
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
