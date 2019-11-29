package dk.aau.cs.gui.smartDraw;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.widgets.CustomJSpinner;

public class SmartDrawDialog extends JDialog {
	private static final long serialVersionUID = 6116530047981607501L;
	
	
	private static String getHelpMessage(){ 
		// There is automatic word wrapping in the control that displays the text, so you don't need line breaks in paragraphs.
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>");
		buffer.append("<b>Automatic net layout options</b><br/>");
		buffer.append("<br/><br/>");
		buffer.append("<b>Search Option</b><br/>\n");
                buffer.append("You can choose between drawing in a Depth First (DFS) or a Breadth First (BFS)\n");
                buffer.append("manner. This may yield a difference as an object is only place once and thus\n");
                buffer.append("reserves its given position. This choice affects which objects are placed first.\n");
		buffer.append("<br/><br/>");
		buffer.append("<b>Functionality</b><br/>");
	        buffer.append("The automatic layout works by choosing a starting object.\n");
	        buffer.append("From there we try different positions around the starting object and choose the position with lowest <em>penalty</em>.\n");
	    buffer.append("<br/><br/>");
	        buffer.append("We try each multiple of x-spacing between <em>parent.x + x-spacing * layer</em> and <em>parent.x - x-spacing * layer</em>\n");
	        buffer.append("<em>parent.x - x-spacing * layer</em> with each multiple of y-spacing\n");
	        buffer.append("between <em>y-spacing * layer</em> and <em>-y-spacing * layer</em>.\n");
	        buffer.append("The value of <em>layer</em> will increment with each iteration and start with value 1.\n");
	        buffer.append("As such choosing the minimum iterations will decide how many layers we try.\n");
	        buffer.append("Minimum Iterations heavily affects how long it will take to do the layout, so a small number is recommended.\n");
	        buffer.append("It is also recommended that x-spacing = y-spacing.\n");
        buffer.append("<br/><br/>");
		buffer.append("<b>Penalties</b><br/>");
		buffer.append("The penalties should be seen as punishments for choosing a position.\n");
                buffer.append("The higher the penalty the higher the punishment; the position with the lowest\n");
                buffer.append("accumulated penalty is where the object will be placed.");
		buffer.append("<br/><br/>");
			buffer.append("<em>Straight arc penalty</em> is a penalty punishing going straight\n");
			buffer.append("i.e. if the candidate position's x or y equals the parent's x or y\n");
			buffer.append("the <em>straight penalty * layer</em> is added to the total penalty.\n");
			buffer.append("If not <em>Diagonal arc penalty * layer</em> is added instead.");
		buffer.append("<br/><br/>");
			buffer.append("<em>Distance penalty</em> punishes candidate positions depending on\n");
			buffer.append("how far away from the starting point they are. As such a higher\n");
			buffer.append("distance penalty will make more compact nets.");
		buffer.append("<br/><br/>");
			buffer.append("<em>Overlapping arc penalty</em> punishes arcs laying directly on top of each other");
			buffer.append("pointing to the same object.");
		buffer.append("<br/><br/>");
			buffer.append("<b>Example:</b><br/>");
		buffer.append("<br/><br/>");
			buffer.append("<img src=\"" + Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "SmartDrawExampleWithLayers.png") +"\" />");
		buffer.append("<br/><br/>");
		buffer.append("This layout was created with the default values.\n");
		buffer.append("On the figure the numbers and boxes describe the layer. Furthermore, the effect of the <em>Overlapping arc penalty</em> can be seen\n");
		buffer.append("as the objects in layer 2 prefer going diagonal rather than overlap due to the penaltys.");
		buffer.append("</html>");
		return buffer.toString(); 
	}
	
	JPanel mainPanel;
	String startingObject = "Random";
	//String template;
	String[] objectNames;
	//String[] templateNames;
	JComboBox<String> objectDropdown = new JComboBox<String>();
	JCheckBox randomStartObjectCheckBox;
	//JComboBox<Object> templateSelector = new JComboBox<Object>();
	JDialog loadingDialogFrame;
	//JDialog choiceModal;
	SmartDrawWorker worker;
	JLabel timerLabel = new JLabel("Time elapsed: ");
	JLabel progressLabel = new JLabel("Objects placed: ");
	JLabel statusLabel;
	JSpinner xSpinner;
	JButton drawButton;
	long startTimeMs;
	int objectsProgress;

	
	int xSpacing = 80;
	int ySpacing = 80;
	String searchOption = "DFS";
	int straightWeight = 5;
	int diagonalWeight = 8;
	int distanceWeight = 10;
	int overlappingArcWeight = 100;
	int minimumIterations = 3;
	
	private Timer timer = new Timer(1000, new AbstractAction() {
		private static final long serialVersionUID = 2126744033604698592L;

		public void actionPerformed(ActionEvent e) {
			timerLabel.setText("Time elapsed: " + (System.currentTimeMillis() - startTimeMs) / 1000 + " s");
		}
	});
	
	public static SmartDrawDialog smartDrawDialog;
	public static void showSmartDrawDialog() {
		
		if(smartDrawDialog == null){
			smartDrawDialog = new SmartDrawDialog(CreateGui.getApp(), "Smart Draw", true);
			smartDrawDialog.pack();
			smartDrawDialog.setPreferredSize(smartDrawDialog.getSize());
			smartDrawDialog.setMinimumSize(new Dimension(smartDrawDialog.getWidth(), smartDrawDialog.getHeight()));
			smartDrawDialog.setLocationRelativeTo(null);
			smartDrawDialog.setResizable(false);
		}
		smartDrawDialog.updateLists();
		smartDrawDialog.enableButtons();
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
		
		/*templateSelector.removeAllItems();
		for(String name : getTemplatesAsString()) {
			templateSelector.addItem(name);
		}*/
	}
	
	private void initComponents() {
		
		setLayout(new FlowLayout());
		mainPanel = new JPanel(new GridBagLayout());
		
		initSpacingSelecters();
		initCheckBoxes();
		initAdvancedOptionsPanel();
		//initChoiceModal();
		
		/*templateSelector.setEnabled(false);
		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(templateSelector, gbc);*/
		JButton helpButton = new JButton("Help");
		helpButton.setToolTipText("Help with the different options");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(CreateGui.getAppGui(), getMessageComponent(), "Help", JOptionPane.INFORMATION_MESSAGE);
				}
			});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		mainPanel.add(helpButton, gbc);
		
		
		drawButton = new JButton("Smart Draw");
		drawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initLoadingFrame();
				worker = new SmartDrawWorker(xSpacing, ySpacing, CreateGui.getDrawingSurface(), searchOption, 
						straightWeight, diagonalWeight, distanceWeight, overlappingArcWeight, startingObject, minimumIterations);
				worker.addSmartDrawListener(new SmartDrawListener() {
					
					@Override
					public void fireStatusChanged(int objectsPlaced) {
						progressLabel.setText("Objects placed: " + objectsPlaced +"/" + CreateGui.getDrawingSurface().getPlaceTransitionObjects().size());
						
					}
					
					@Override
					public void fireStartDraw() {
						statusLabel.setText("Working...");
						if (timer.isRunning())
							timer.restart();
						else
							timer.start();
						startTimeMs = System.currentTimeMillis();
						
					}
					
					@Override
					public void fireDone(boolean cancelled) {
						if(!(cancelled)) {
							loadingDialogFrame.dispose();
							CreateGui.getAppGui().toFront();
							CreateGui.getAppGui().requestFocus();
						} else {
							statusLabel.setText("Cancelling/Undoing");
						}
					}
				});
				worker.execute();
				smartDrawDialog.setVisible(false);
				loadingDialogFrame.setVisible(true);
				/*loadingDialogFrame.toFront();
				loadingDialogFrame.requestFocus();
				loadingDialogFrame.setAlwaysOnTop(true);*/
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
		
		
		this.getRootPane().setDefaultButton(drawButton);
		drawButton.requestFocus();
		
		setContentPane(mainPanel);
	}
	
	private void initAdvancedOptionsPanel() {
		JPanel advancedOptionsPanel = new JPanel(new GridBagLayout());
		advancedOptionsPanel.setBorder(new TitledBorder("Advanced Options"));
		
		JLabel comboBoxLabel = new JLabel("Choose Initial Object:");
		comboBoxLabel.setToolTipText("Choose a starting  object");
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
		objectDropdown.setToolTipText("Choose a starting object");
		objectDropdown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(objectDropdown.getSelectedItem() != null && objectDropdown.isEnabled())
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
		
		
		JLabel straightWeightLabel = new JLabel("Straight Arc Penalty:");
		straightWeightLabel.setToolTipText("Higher number decreases the number of horizontal and vertical arcs");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(straightWeightLabel, gbc);
		
		final JSpinner straightWeightSpinner = new CustomJSpinner(straightWeight);
		straightWeightSpinner.setToolTipText("Higher number decreases the number of horizontal and vertical arcs");
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
		
		JLabel diagonalWeightLabel = new JLabel("Diagonal Arc Penalty:");
		diagonalWeightLabel.setToolTipText("Higher number decreases the number of diagonal arcs");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(diagonalWeightLabel, gbc);
		
		final JSpinner diagonalWeightSpinner = new CustomJSpinner(diagonalWeight);
		diagonalWeightSpinner.setToolTipText("Higher number decreases the number of diagonal arcs");
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
		
		JLabel distanceWeightLabel = new JLabel("Distance Penalty:");
		distanceWeightLabel.setToolTipText("Higher penalty will make the layout more compact");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(distanceWeightLabel, gbc);
		
		final JSpinner distanceWeightSpinner = new CustomJSpinner(distanceWeight);
		distanceWeightSpinner.setToolTipText("Higher penalty will make the layout more compact");
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
		
		JLabel overlappingWeightLabel = new JLabel("Overlapping Arc Penalty:");
		overlappingWeightLabel.setToolTipText("Higher penalty will decrease the number of arcs that cross other objects");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(overlappingWeightLabel, gbc);
		
		final JSpinner overlappingWeightSpinner = new CustomJSpinner(overlappingArcWeight);
		overlappingWeightSpinner.setToolTipText("Higher penalty will decrease the number of arcs that cross other objects");
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
		
		JLabel minimumIterationsLabel = new JLabel("Minimum Iterations:");
		minimumIterationsLabel.setToolTipText("Higher number increases the number of positions tried for each object");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(minimumIterationsLabel, gbc);
		
		final JSpinner minimumIterationSpinner = new CustomJSpinner(minimumIterations);
		minimumIterationSpinner.setToolTipText("Higher number increases the number of positions tried for each object");
		minimumIterationSpinner.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				minimumIterations = (Integer) minimumIterationSpinner.getValue();
			}
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		advancedOptionsPanel.add(minimumIterationSpinner, gbc);
		
		
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
		DFS.setToolTipText("Draw in a depth first manner from start object");
		DFS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchOption = "DFS";
			}
		});
		JRadioButton BFS = new JRadioButton("BFS:");
		BFS.setToolTipText("Draw in a breadth first manner from start object");

		BFS.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchOption = "BFS";
			}
		});
		
		/*JRadioButton randomSearch = new JRadioButton("Random:");
		randomSearch.setEnabled(false);
		randomSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchOption = "Random";
			}
		});*/
		
	    ButtonGroup group = new ButtonGroup();
	    group.add(DFS);
	    group.add(BFS);
	    //group.add(randomSearch);
	    
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
		
		/*randomSearch.setHorizontalTextPosition(SwingConstants.LEFT);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		checkBoxPanel.add(randomSearch, gbc);*/
		
		
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
		xLabel.setToolTipText("Set the distance there should be between objects on the x-axis");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		spacingPanel.add(xLabel, gbc);
		
		xSpinner = new CustomJSpinner(xSpacing);
		xSpinner.setToolTipText("Set the distance there should be between objects on the x-axis");

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
		yLabel.setToolTipText("Set the distance there should be between objects on the y-axis");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		spacingPanel.add(yLabel, gbc);
		
		final JSpinner ySpinner = new CustomJSpinner(ySpacing);
		ySpinner.setToolTipText("Set the distance there should be between objects on the y-axis");
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
	
	private void initLoadingFrame() {
		loadingDialogFrame = new JDialog(CreateGui.getApp(), "Working...", true);
		loadingDialogFrame.setLayout(new GridBagLayout());
		loadingDialogFrame.setType(Window.Type.POPUP);
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
		
		
		statusLabel = new JLabel("Working... ", loadingGIF, JLabel.CENTER);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		loadingDialogFrame.add(statusLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 10, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		loadingDialogFrame.add(timerLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 10, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		loadingDialogFrame.add(progressLabel, gbc);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelWorker();
				CreateGui.getDrawingSurface().getUndoManager().undo();
				CreateGui.getDrawingSurface().repaintAll();
				loadingDialogFrame.setVisible(false);
				//smartDrawDialog.setVisible(true);
				CreateGui.getAppGui().toFront();
				CreateGui.getAppGui().requestFocus();
				
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTH;
		loadingDialogFrame.add(cancelButton, gbc);
		

		loadingDialogFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		loadingDialogFrame.setSize(400, 300);
		loadingDialogFrame.setVisible(false);
		loadingDialogFrame.setLocationRelativeTo(CreateGui.getAppGui());
		loadingDialogFrame.pack();
	}
	
	
	
	
	/*
	 * Asks if you want to keep, revert or try again
	 * is not used currently
	 */
	/*private void initChoiceModal() {
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
		
	}*/
	
	private Object getMessageComponent(){
		JTextPane pane = new JTextPane();
		pane.setContentType("text/html");
		pane.setText(getHelpMessage());
		pane.setEditable(false);
		pane.setCaretPosition(0);
		for(MouseListener listener : pane.getMouseListeners()){
			pane.removeMouseListener(listener);
		}
		Dimension dim = new Dimension(500,400);
		pane.setPreferredSize(dim);  
		pane.setMargin(new Insets(5,5,5,5));  
		JScrollPane scrollPane = new JScrollPane(pane);  
		scrollPane.setPreferredSize(dim);  
		return scrollPane;  
	}

	
	private void cancelWorker() {
		if (worker != null && !worker.isDone()) {
			boolean cancelled = false;
			do {
				cancelled = worker.cancel(true);
			} while (!cancelled);
		}
	}
	
	private void enableButtons() {
		if(CreateGui.getDrawingSurface().getPlaceTransitionObjects().size() > 0) {
			drawButton.setEnabled(true);
			drawButton.setToolTipText("Smart draw with the current options");
			if(!(randomStartObjectCheckBox.isSelected()))
				objectDropdown.setEnabled(true);
			
			randomStartObjectCheckBox.setEnabled(true);
		} else {
			drawButton.setEnabled(false);
			drawButton.setToolTipText("You must have at least one object to smart draw");
			objectDropdown.setEnabled(false);
			randomStartObjectCheckBox.setEnabled(false);
		}
	}
}
