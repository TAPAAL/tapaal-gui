package dk.aau.cs.gui.smartDraw;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileView;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;

public class SmartDrawDialog extends JDialog {
	private static final long serialVersionUID = 6116530047981607501L;
	
	JPanel mainPanel;
	ArrayList<PlaceTransitionObject> placeTransitionObjects;
	String startingObject;
	String template;
	String[] objectNames;
	String[] templateNames;
	JComboBox objectDropdown = new JComboBox();
	JCheckBox randomStartObjectCheckBox;
	JComboBox templateSelector = new JComboBox();
	JDialog loadingDialogFrame;
	Thread workingThread;
	Thread loadingDialogFrameThread;
	JDialog choiceModal;
	boolean cancel = false;
	SmartDrawWorker worker;

	
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
		smartDrawDialog.updateLists();
		smartDrawDialog.setEnabled(true);
		smartDrawDialog.setVisible(true);
	}

	private SmartDrawDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
		initComponents();
	}
	
	private void updateLists() {
		objectDropdown.removeAllItems();
		for(String name : getObjectNames()) {
			objectDropdown.addItem(name);
		}
		
		templateSelector.removeAllItems();
		for(String name : getTemplatesAsString()) {
			templateSelector.addItem(name);
		}
	}
	
	private void initComponents() {
		
		setLayout(new FlowLayout());
		mainPanel = new JPanel(new GridBagLayout());
		
		initSpacingSelecters();
		initCheckBoxes();
		initAdvancedOptionsPanel();
		initChoiceModal();
		initLoadingFrame();
		
		templateSelector.setEnabled(false);
		
		
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
				cancel = false;
				worker = new SmartDrawWorker(xSpacing, ySpacing, CreateGui.getDrawingSurface(), searchOption, 
						straightWeight, diagonalWeight, distanceWeight, overlappingArcWeight, objectDropdown.getSelectedItem().toString());
				loadingDialogFrameThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						loadingDialogFrame.setVisible(true);
						
					}
				});
				loadingDialogFrameThread.start();
				smartDrawDialog.setVisible(false);
				workingThread = new Thread(new Runnable() {
					@Override
					public void run() {
						worker.smartDraw();
					}
				});
				workingThread.run();
				if(worker.isDone() && cancel == false) {
					loadingDialogFrame.setVisible(false);
					choiceModal.setVisible(true);
				}
				
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
		
		objectDropdown.setEnabled(false);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(objectDropdown, gbc);
		
		randomStartObjectCheckBox = new JCheckBox("Random Initial Object:", true);
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
		randomSearch.setEnabled(false);
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
		
		SpinnerModel xSpaceModel =
		        new SpinnerNumberModel(xSpacing, //initial value
		                               0, //min
		                               Integer.MAX_VALUE, //max
		                               1);
		final JSpinner xSpinner = new JSpinner(xSpaceModel);
		xSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				xSpacing = (Integer)xSpinner.getValue();
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
		spacingPanel.add(xSpinner, gbc);
		
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
		
		SpinnerModel ySpaceModel =
		        new SpinnerNumberModel(ySpacing, //initial value
		                               0, //min
		                               Integer.MAX_VALUE, //max
		                               1);
		final JSpinner ySpinner = new JSpinner(ySpaceModel);
		ySpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ySpacing = (Integer)ySpinner.getValue();
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
		spacingPanel.add(ySpinner, gbc);
		
		
		
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
	
	static private String[] getObjectNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(PetriNetObject object : CreateGui.getDrawingSurface().getPlaceTransitionObjects()) {
			names.add(object.getName());
		}
		return Arrays.copyOf(names.toArray(), names.toArray().length, String[].class);
	}
	//For debugging
	private void printPTObjectsAndPositions() {
		for(PlaceTransitionObject ptObject : placeTransitionObjects) {
			System.out.println("Name: " + ptObject.getName() + " X: " + ptObject.getPositionX() + " Y: " + ptObject.getPositionY());
		}
	}
	
	private void initLoadingFrame() {
		loadingDialogFrame = new JDialog(smartDrawDialog, "Working...", true);
		loadingDialogFrame.setLayout(new GridBagLayout());
		ImageIcon loadingGIF = new ImageIcon(CreateGui.imgPath + "ajax-loader.gif");
		
		JLabel workingLabel = new JLabel("<html><div style='text-align: center;'>Currently doing layout...<br/>This may take several minutes depending on the size of the net...</div></html>", SwingConstants.CENTER);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		
		loadingDialogFrame.add(workingLabel, gbc);
		JLabel loading = new JLabel("Working... ", loadingGIF, JLabel.CENTER);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		loadingDialogFrame.add(loading, gbc);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				while(!(workingThread.isInterrupted())) {
					workingThread.interrupt();
					if(workingThread.isInterrupted()) {
						CreateGui.getDrawingSurface().getUndoManager().undo();
						CreateGui.getDrawingSurface().repaintAll();
						loadingDialogFrame.setVisible(false);
						smartDrawDialog.setEnabled(true);
						cancel = true;
					}
				}
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		loadingDialogFrame.add(cancelButton, gbc);
		

		loadingDialogFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		loadingDialogFrame.setSize(400, 300);
		loadingDialogFrame.setVisible(false);
		loadingDialogFrame.setLocationRelativeTo(smartDrawDialog);
	}
	
	private void initChoiceModal() {
		choiceModal = new JDialog(smartDrawDialog, "Keep?", true);
		choiceModal.setLayout(new GridBagLayout());
		choiceModal.setVisible(false);
		choiceModal.setLocationRelativeTo(smartDrawDialog);
		choiceModal.setResizable(false);
		choiceModal.setSize(300, 100);

		
		JLabel choiceLabel = new JLabel("<html><div style='text-align: center;'>Would you like to keep the new layout,<br/> revert or try again?</div></html>", SwingConstants.CENTER);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTH;
		
		choiceModal.add(choiceLabel, gbc);
		
		JButton keepButton = new JButton("Keep layout");
		keepButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				smartDrawDialog.setVisible(false);
				choiceModal.setVisible(false);				
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		choiceModal.add(keepButton, gbc);
		
		JButton revertButton = new JButton("Revert");
		revertButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateGui.getDrawingSurface().getUndoManager().undo();
				smartDrawDialog.setVisible(false);
				choiceModal.setVisible(false);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		choiceModal.add(revertButton, gbc);
		
		JButton retryButton = new JButton("Try Again");
		retryButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				choiceModal.setVisible(false);
				smartDrawDialog.setVisible(true);
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		choiceModal.add(retryButton, gbc);
		
		
	}

    
	
}
