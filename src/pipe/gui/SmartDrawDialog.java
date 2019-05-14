package pipe.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MovePlaceTransitionObject;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPathPoint;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.undo.DeleteArcPathPointEdit;
import pipe.gui.widgets.CustomJSpinner;

public class SmartDrawDialog extends JDialog {
	private static final long serialVersionUID = 6116530047981607501L;
	
	JPanel mainPanel;
	ArrayList<PlaceTransitionObject> placeTransitionObjects;
	pipe.gui.undo.UndoManager undoManager;
	ArrayList<Point> pointsReserved;

	//For BFS
	ArrayList<PlaceTransitionObject> newlyPlacedObjects;
	ArrayList<PlaceTransitionObject> objectsPlaced;
	
	//For DFS
	ArrayList<PlaceTransitionObject> unfinishedObjects;
	ArrayList<Arc> arcsVisited;

	
	int xSpacing = 50;
	int ySpacing = 50;
	String searchOption = "DFS";
	
	static SmartDrawDialog smartDrawDialog;
	public static void showSmartDrawDialog() {
		if(smartDrawDialog == null){
			smartDrawDialog = new SmartDrawDialog(CreateGui.getApp(), "Smart Draw", true);
			smartDrawDialog.pack();
			smartDrawDialog.setPreferredSize(smartDrawDialog.getSize());
			smartDrawDialog.setMinimumSize(new Dimension(smartDrawDialog.getWidth(), smartDrawDialog.getHeight()));
			smartDrawDialog.setLocationRelativeTo(null);
			smartDrawDialog.setResizable(true);
		}
		
		smartDrawDialog.setVisible(true);
	}

	private SmartDrawDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		initComponents();
	}
	
	private void initComponents() {
		
		setLayout(new FlowLayout());
		mainPanel = new JPanel(new GridBagLayout());
		
		initSpacingSelecters();
		initCheckBoxes();
		
		
		JButton drawButton = new JButton("Smart Draw");
		drawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				undoManager = CreateGui.getDrawingSurface().getUndoManager();
				getPlaceTransitionObjects();
				newlyPlacedObjects = new ArrayList<PlaceTransitionObject>();
				pointsReserved = new ArrayList<Point>();
				smartDraw();
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(drawButton, gbc);
		
		setContentPane(mainPanel);
	}
	
	private void initCheckBoxes() {
		JPanel checkBoxPanel = new JPanel(new GridBagLayout());
		checkBoxPanel.setBorder(new TitledBorder("Search Option"));
		
		JRadioButton DFS = new JRadioButton("DFS:");
		DFS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchOption = "DFS";
			}
		});
		JRadioButton BFS = new JRadioButton("BFS:");
		BFS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchOption = "BFS";
			}
		});
		
	    ButtonGroup group = new ButtonGroup();
	    group.add(DFS);
	    group.add(BFS);
	    
	    DFS.setSelected(true);
	    DFS.setHorizontalTextPosition(SwingConstants.LEFT);
	    GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		checkBoxPanel.add(DFS, gbc);
		
	    BFS.setHorizontalTextPosition(SwingConstants.LEFT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		checkBoxPanel.add(BFS, gbc);
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(checkBoxPanel, gbc);

	}
	
	private void initSpacingSelecters(){
		JPanel spacingPanel = new JPanel(new GridBagLayout());
		spacingPanel.setBorder(new TitledBorder("Spacing"));
		
		
		JLabel xLabel = new JLabel("Spacing on the x-axis:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		spacingPanel.add(xLabel, gbc);
		
		
		final JSpinner xSpaceSpinner = new CustomJSpinner(xSpacing, 50, Integer.MAX_VALUE);
		xSpaceSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				xSpacing = (Integer)xSpaceSpinner.getValue();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 10, 10, 10);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		spacingPanel.add(xSpaceSpinner, gbc);
		
		JLabel yLabel = new JLabel("Spacing on the y-axis:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		spacingPanel.add(yLabel, gbc);
		
		final JSpinner ySpaceSpinner = new CustomJSpinner(ySpacing, 50, Integer.MAX_VALUE);
		ySpaceSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ySpacing = (Integer)ySpaceSpinner.getValue();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 10, 10, 10);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		spacingPanel.add(ySpaceSpinner, gbc);
		
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(spacingPanel, gbc);
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
		for(PetriNetObject object : CreateGui.getDrawingSurface().getPlaceTransitionObjects()) {
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
