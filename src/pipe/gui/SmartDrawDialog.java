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
import java.util.Random;

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
import javax.swing.undo.UndoManager;

import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MovePlaceTransitionObject;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.verification.TAPNComposer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.widgets.CustomJSpinner;

public class SmartDrawDialog extends JDialog {
	private static final long serialVersionUID = 6116530047981607501L;
	
	JPanel mainPanel;
	ArrayList<PetriNetObject> drawingSurfaceObjects;
	pipe.gui.undo.UndoManager undoManager;
	ArrayList<PlaceTransitionObject> newlyPlacedObjects;
	ArrayList<Point> pointsReserved;
	
	int xSpacing = 100;
	int ySpacing = 100;
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
				drawingSurfaceObjects = CreateGui.getDrawingSurface().getPlaceTransitionObjects();
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
		PlaceTransitionObject startingObject = (PlaceTransitionObject) drawingSurfaceObjects.get(0);
		Point startingPoint = new Point(500,350);
		Command command = new MovePlaceTransitionObject(startingObject, startingPoint);
		command.redo();
		undoManager.addEdit(command);
		pointsReserved.add(startingPoint);
		newlyPlacedObjects.add(startingObject);
		
		if(searchOption == "DFS") {
			depthFirstDraw(startingObject);
		} else {
			while(!(newlyPlacedObjects.isEmpty())) {
				PlaceTransitionObject newParent = newlyPlacedObjects.get(0);
				breadthFirstDraw(newParent);
			}
		}
		
		CreateGui.getDrawingSurface().repaintAll();
		CreateGui.getModel().repaintAll(true);
		CreateGui.getDrawingSurface().updatePreferredSize();
		System.out.println(xSpacing + " " + ySpacing);
		System.out.println(searchOption);
	}
	
	private void depthFirstDraw(PlaceTransitionObject startingObject) {
		
	}
	
	private void breadthFirstDraw(PlaceTransitionObject parentObject) {
		Iterator<Arc> arcFromIterator = parentObject.getConnectFromIterator();
		Command command;
		boolean objectPlaced = false;
		while(arcFromIterator.hasNext()) {
			PlaceTransitionObject objectToPlace = arcFromIterator.next().getTarget();
			objectPlaced = false;
			int perimeterLevel = 0;
			while(!objectPlaced) {
				perimeterLevel += 1;
				outerloop: for(int x = ((int)parentObject.getPositionX() - (xSpacing*perimeterLevel)); x <= ((int)parentObject.getPositionX() + (xSpacing*perimeterLevel)); x += xSpacing) {
					for(int y = ((int)parentObject.getPositionY() - (ySpacing * perimeterLevel)); y <= ((int)parentObject.getPositionY() + (ySpacing*perimeterLevel)); y += ySpacing) {
						Point possiblePoint = new Point(x, y);
						if(!(pointsReserved.contains(possiblePoint))) {
							command = new MovePlaceTransitionObject(objectToPlace, possiblePoint);
							command.redo();
							undoManager.addEdit(command);
							pointsReserved.add(possiblePoint);
							newlyPlacedObjects.add(objectToPlace);
							objectPlaced = true;
							break outerloop;
						}
					}
				}
			}
		}
		newlyPlacedObjects.remove(parentObject);
	}
	
}
