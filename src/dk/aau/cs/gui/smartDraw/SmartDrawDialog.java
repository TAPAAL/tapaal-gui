package dk.aau.cs.gui.smartDraw;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.widgets.CustomJSpinner;

public class SmartDrawDialog extends JDialog {
	private static final long serialVersionUID = 6116530047981607501L;
	
	JPanel mainPanel;
	ArrayList<PlaceTransitionObject> placeTransitionObjects;
	pipe.gui.undo.UndoManager undoManager;
	ArrayList<Point> pointsReserved;
	String startingObject;
	String template;

	//For BFS
	ArrayList<PlaceTransitionObject> newlyPlacedObjects;
	ArrayList<PlaceTransitionObject> objectsPlaced;
	
	//For DFS
	ArrayList<PlaceTransitionObject> unfinishedObjects;
	ArrayList<Arc> arcsVisited;

	
	int xSpacing = 80;
	int ySpacing = 80;
	String searchOption = "DFS";
	int straightWeight = 5;
	int diagonalWeight = 8;
	int distanceWeight = 10;
	int overlappingArcWeight = 100;
	
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
		initAdvancedOptionsPanel();
		
		final JComboBox templateSelector = new JComboBox(getTemplatesAsString());
		templateSelector.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				template = templateSelector.getSelectedItem().toString();
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(templateSelector, gbc);
		
		
		JButton drawButton = new JButton("Smart Draw");
		drawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SmartDrawWorker worker = new SmartDrawWorker(xSpacing, ySpacing, CreateGui.getDrawingSurface(), searchOption, 
						straightWeight, diagonalWeight, distanceWeight, overlappingArcWeight, startingObject);
				worker.smartDraw();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		mainPanel.add(drawButton, gbc);
		
		setContentPane(mainPanel);
	}
	
	private void initAdvancedOptionsPanel() {
		JPanel advancedOptionsPanel = new JPanel(new GridBagLayout());
		advancedOptionsPanel.setBorder(new TitledBorder("Advanced Options"));
		
		String[] objectNames = {"Choose object"};
		int i = 1;
		for(PetriNetObject object : CreateGui.getDrawingSurface().getPlaceTransitionObjects()) {
			objectNames[i] = object.getName();
			i++;
		}
		JLabel comboBoxLabel = new JLabel("Choose Initial Object:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(comboBoxLabel, gbc);
		
		final JComboBox objectDropdown = new JComboBox(objectNames);
		objectDropdown.setSelectedIndex(0);
		objectDropdown.setEnabled(false);
		objectDropdown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				startingObject = objectDropdown.getSelectedItem().toString();
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(objectDropdown, gbc);
		
		final JCheckBox randomStartObjectCheckBox = new JCheckBox("Random Initial Object:", true);
		randomStartObjectCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
		randomStartObjectCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(randomStartObjectCheckBox.isSelected()) {
					startingObject = "Random";
					objectDropdown.setEnabled(false);
				} else {
					objectDropdown.setEnabled(true);
					startingObject = objectDropdown.getSelectedItem().toString();
				}
					
				
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(randomStartObjectCheckBox, gbc);
		
		
		JLabel straightWeightLabel = new JLabel("Straight Arc Weight:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(straightWeightLabel, gbc);
		
		
		SpinnerModel straightWeightModel =
		        new SpinnerNumberModel(straightWeight, //initial value
		                               0, //min
		                               Integer.MAX_VALUE, //max
		                               1);
		final JSpinner straightWeightSpinner = new JSpinner(straightWeightModel);
		straightWeightSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				straightWeight = (Integer) straightWeightSpinner.getValue();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(straightWeightSpinner, gbc);
		
		JLabel diagonalWeightLabel = new JLabel("Diagonal Arc Weight:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(diagonalWeightLabel, gbc);
		
		
		SpinnerModel diagonalWeightModel =
		        new SpinnerNumberModel(diagonalWeight, //initial value
		                               0, //min
		                               Integer.MAX_VALUE, //max
		                               1);
		final JSpinner diagonalWeightSpinner = new JSpinner(diagonalWeightModel);
		diagonalWeightSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				diagonalWeight = (Integer) diagonalWeightSpinner.getValue();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(diagonalWeightSpinner, gbc);
		
		JLabel distanceWeightLabel = new JLabel("Distance Weight:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(distanceWeightLabel, gbc);
		
		
		SpinnerModel distanceWeightModel =
		        new SpinnerNumberModel(distanceWeight, //initial value
		                               0, //min
		                               Integer.MAX_VALUE, //max
		                               1);
		final JSpinner distanceWeightSpinner = new JSpinner(distanceWeightModel);
		distanceWeightSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				distanceWeight = (Integer) distanceWeightSpinner.getValue();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(distanceWeightSpinner, gbc);
		
		JLabel overlappingWeightLabel = new JLabel("Overlapping Arc Weight:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(overlappingWeightLabel, gbc);
		
		
		SpinnerModel overlappingWeightModel =
		        new SpinnerNumberModel(overlappingArcWeight, //initial value
		                               0, //min
		                               Integer.MAX_VALUE, //max
		                               1);
		final JSpinner overlappingWeightSpinner = new JSpinner(overlappingWeightModel);
		overlappingWeightSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				overlappingArcWeight = (Integer) overlappingWeightSpinner.getValue();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(overlappingWeightSpinner, gbc);
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridheight = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(advancedOptionsPanel, gbc);
		
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
		JRadioButton randomSearch = new JRadioButton("Random:");
		BFS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchOption = "Random";
			}
		});
		
	    ButtonGroup group = new ButtonGroup();
	    group.add(DFS);
	    group.add(BFS);
	    group.add(randomSearch);
	    
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
		
		randomSearch.setHorizontalTextPosition(SwingConstants.LEFT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		checkBoxPanel.add(randomSearch, gbc);
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
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
		
		
		final JSpinner xSpaceSpinner = new CustomJSpinner(xSpacing, 0, Integer.MAX_VALUE);
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
		
		final JSpinner ySpaceSpinner = new CustomJSpinner(ySpacing, 0, Integer.MAX_VALUE);
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
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(spacingPanel, gbc);
	}
	
	private String[] getTemplatesAsString() {
		String[] templateNames = {"Choose Template"};
		Iterator<pipe.dataLayer.Template> iterator = CreateGui.getCurrentTab().activeTemplates().iterator();
		int i = 0;
		while(iterator.hasNext()) {
			pipe.dataLayer.Template template = iterator.next();
			templateNames[i] = template.model().name();
		}
		return templateNames;
	}
	//For debugging
	private void printPTObjectsAndPositions() {
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			System.out.println("Name: " + ptObject.getName() + " X: " + ptObject.getPositionX() + " Y: " + ptObject.getPositionY());
		}
	}
	
}
