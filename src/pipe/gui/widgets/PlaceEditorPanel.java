package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TimedPlaceComponent;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import dk.aau.cs.TCTL.visitors.RenamePlaceTCTLVisitor;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.ChangedInvariantCommand;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MakePlaceSharedCommand;
import dk.aau.cs.gui.undo.RenameTimedPlaceCommand;
import dk.aau.cs.gui.undo.TimedPlaceMarkingEdit;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.RequireException;

public class PlaceEditorPanel extends javax.swing.JPanel {
	private static final long serialVersionUID = -4163767112591119036L;
	private JRootPane rootPane;
	
	private JCheckBox sharedCheckBox;
	private JComboBox sharedPlacesComboBox;

	private TimedPlaceComponent place;
	private Context context;
	
	public PlaceEditorPanel(JRootPane rootPane, TimedPlaceComponent placeComponent, Context context) {
		this.rootPane = rootPane;
		this.place = placeComponent;
		this.context = context;
		initComponents();
	}

	private void initComponents() {
		setLayout(new java.awt.GridBagLayout());
		
		initBasicPropertiesPanel();
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(5, 8, 0, 8);
		add(basicPropertiesPanel, gridBagConstraints);

		initTimeInvariantPanel();
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 8, 0, 8);
		add(timeInvariantPanel, gridBagConstraints);

		initButtonPanel();
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 8, 5, 8);
		add(buttonPanel, gridBagConstraints);
	}

	private void initButtonPanel() {
		java.awt.GridBagConstraints gridBagConstraints;
		buttonPanel = new javax.swing.JPanel();
		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton = new javax.swing.JButton();
		okButton.setText("OK");

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				doOK();
			}
		});
		rootPane.setDefaultButton(okButton);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		buttonPanel.add(okButton, gridBagConstraints);

		cancelButton = new javax.swing.JButton();
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exit();
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		buttonPanel.add(cancelButton, gridBagConstraints);
	}

	private void initBasicPropertiesPanel() {
		basicPropertiesPanel = new JPanel();
		basicPropertiesPanel.setLayout(new java.awt.GridBagLayout());
		basicPropertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Place"));

		sharedCheckBox = new JCheckBox("Shared");
		sharedCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox box = (JCheckBox)arg0.getSource();
				if(box.isSelected()){
					switchToNameDropDown();
				}else{
					switchToNameTextField();
				}
			}		
		});
		sharedCheckBox.setEnabled(context.network().numberOfSharedPlaces() > 0);
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
		basicPropertiesPanel.add(sharedCheckBox, gridBagConstraints);
		
		sharedPlacesComboBox = new JComboBox(context.network().sharedPlaces().toArray());
		
		nameLabel = new javax.swing.JLabel();
		nameLabel.setText("Name:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(nameLabel, gridBagConstraints);

		nameTextField = new javax.swing.JTextField();
		nameTextField.setText(place.getName());

		markingLabel = new javax.swing.JLabel();
		markingLabel.setText("Marking:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(markingLabel, gridBagConstraints);

		markingSpinner = new javax.swing.JSpinner();
		markingSpinner.setModel(new SpinnerNumberModel(place.getNumberOfTokens(), 0, Integer.MAX_VALUE, 1));
		markingSpinner.setPreferredSize(new Dimension(50,27));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(markingSpinner, gridBagConstraints);

		attributesCheckBox = new javax.swing.JCheckBox();
		attributesCheckBox.setSelected(place.getAttributesVisible());
		attributesCheckBox.setText("Show place attributes");
		attributesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		attributesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(attributesCheckBox, gridBagConstraints);
		
		if(place.underlyingPlace().isShared()){
			switchToNameDropDown();
			sharedCheckBox.setSelected(true);
			sharedPlacesComboBox.setSelectedItem(place.underlyingPlace());
		}else{
			switchToNameTextField();
		}
	}

	private void initTimeInvariantPanel() {
		timeInvariantPanel = new JPanel();
		timeInvariantPanel.setLayout(new java.awt.GridBagLayout());
		timeInvariantPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Time Invariant"));

		invariantGroup = new JPanel(new GridBagLayout());
		invRelationNormal = new JComboBox(new String[] { "<=", "<" });
		invRelationConstant = new JComboBox(new String[] { "<=", "<" });
		invariantSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		invariantSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if ((Integer) invariantSpinner.getValue() < 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
					invRelationNormal.setSelectedItem("<=");
				} else if (invRelationNormal.getModel().getSize() == 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
				}

			}

		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		invariantGroup.add(invRelationNormal, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		invariantGroup.add(invRelationConstant, gbc);

		invariantSpinner.setMaximumSize(new Dimension(80, 30));
		invariantSpinner.setMinimumSize(new Dimension(80, 30));
		invariantSpinner.setPreferredSize(new Dimension(80, 30));

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(3, 3, 3, 3);
		invariantGroup.add(invariantSpinner, gbc);

		invariantInf = new JCheckBox("inf");
		invariantInf.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (!invariantInf.isSelected()) {
					invariantSpinner.setEnabled(true);
					invRelationNormal.setSelectedItem("<=");
					if ((Integer) invariantSpinner.getValue() < 1) {
						invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
					} else {
						invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
					}
				} else {
					invariantSpinner.setEnabled(false);
					invRelationNormal.setSelectedItem("<");
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<" }));
				}

			}

		});
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 0;
		invariantGroup.add(invariantInf, gbc);

		Set<String> constants = context.network().getConstantNames();
		invConstantsComboBox = new JComboBox(constants.toArray());

		invConstantsComboBox.setMinimumSize(new Dimension(80, 30));
		invConstantsComboBox.setPreferredSize(new Dimension(80, 30));
		invConstantsComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setRelationModelForConstants();
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		invariantGroup.add(invConstantsComboBox, gbc);

		normalInvRadioButton = new JRadioButton("Normal");
		normalInvRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disableInvariantComponents();
				enableNormalInvariantComponents();
			}
		});

		constantInvRadioButton = new JRadioButton("Constant");
		constantInvRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disableInvariantComponents();
				enableConstantInvariantComponents();
			}
		});
		if (constants.isEmpty()){
			constantInvRadioButton.setEnabled(false);
		}
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(normalInvRadioButton);
		btnGroup.add(constantInvRadioButton);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		invariantGroup.add(normalInvRadioButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		invariantGroup.add(constantInvRadioButton, gbc);

		TimeInvariant invariantToSet = place.getInvariant();

		if (invariantToSet.isUpperNonstrict()) {
			invRelationNormal.setSelectedItem("<=");
		} else {
			invRelationNormal.setSelectedItem("<");
		}

		if (invariantToSet.upperBound() instanceof InfBound) {
			invariantSpinner.setEnabled(false);
			invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<" }));
			invariantInf.setSelected(true);
			invRelationNormal.setSelectedItem("<");
		}

		disableInvariantComponents();
		if (invariantToSet.upperBound() instanceof ConstantBound) {
			enableConstantInvariantComponents();
			constantInvRadioButton.setSelected(true);
			invConstantsComboBox.setSelectedItem(((ConstantBound) invariantToSet.upperBound()).name());
			invRelationConstant.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
		} else {
			enableNormalInvariantComponents();
			normalInvRadioButton.setSelected(true);
			if (invariantToSet.upperBound() instanceof IntBound) {
				if ((Integer) invariantSpinner.getValue() < 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
				} else {
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
				}
				invariantSpinner.setValue(invariantToSet.upperBound().value());
				invariantSpinner.setEnabled(true);
				invRelationNormal.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
				invariantInf.setSelected(false);
			}
		}

		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		timeInvariantPanel.add(invariantGroup, gridBagConstraints);
	}

	private void setRelationModelForConstants() {
		int value = CreateGui.getCurrentTab().network().getConstantValue(invConstantsComboBox.getSelectedItem().toString());

		String selected = invRelationConstant.getSelectedItem().toString();
		if (value == 0) {
			invRelationConstant.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
		} else {
			invRelationConstant.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
		}
		invRelationConstant.setSelectedItem(selected);
	}

	protected void enableConstantInvariantComponents() {
		invRelationConstant.setEnabled(true);
		invConstantsComboBox.setEnabled(true);
		setRelationModelForConstants();
	}

	protected void enableNormalInvariantComponents() {
		invRelationNormal.setEnabled(true);
		invariantInf.setEnabled(true);
		invariantSpinner.setValue(0);
		invariantInf.setSelected(true);
		invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<" }));
	}

	protected void disableInvariantComponents() {
		invRelationNormal.setEnabled(false);
		invRelationConstant.setEnabled(false);
		invariantSpinner.setEnabled(false);
		invConstantsComboBox.setEnabled(false);
		invariantInf.setEnabled(false);
	}

	ChangeListener changeListener = new javax.swing.event.ChangeListener() {
		public void stateChanged(javax.swing.event.ChangeEvent evt) {
			JSpinner spinner = (JSpinner) evt.getSource();
			JSpinner.NumberEditor numberEditor = ((JSpinner.NumberEditor) spinner.getEditor());
			numberEditor.getTextField().setBackground(new Color(255, 255, 255));
			spinner.removeChangeListener(this);
		}
	};
	
	private void switchToNameTextField() {
		basicPropertiesPanel.remove(sharedPlacesComboBox);
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(nameTextField, gbc);	
		basicPropertiesPanel.validate();
		basicPropertiesPanel.repaint();
	}

	private void switchToNameDropDown() {
		basicPropertiesPanel.remove(nameTextField);
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(sharedPlacesComboBox, gbc);		
		basicPropertiesPanel.validate();
		basicPropertiesPanel.repaint();
	}

	private void doOK() {
		context.undoManager().newEdit(); // new "transaction""
		
		SharedPlace selectedPlace = (SharedPlace)sharedPlacesComboBox.getSelectedItem();
		boolean wasShared = place.underlyingPlace().isShared() && !sharedCheckBox.isSelected();
//		if(place.underlyingPlace().isShared()){
//			view.getUndoManager().addEdit(new UnsharePlaceCommand(place.underlyingPlace().sharedPlace(), place.underlyingPlace()));
//			place.underlyingPlace().unshare();
//		}
//		
		if(sharedCheckBox.isSelected()){ // If you make it selected, everything else (marking, invariant) is disregarded
			Command command = new MakePlaceSharedCommand(context.activeModel(), selectedPlace, place.underlyingPlace(), place); // TODO: avoid casting
			context.undoManager().addEdit(command);
			try{
				command.redo();
			}catch(RequireException e){
				context.undoManager().undo();
				JOptionPane.showMessageDialog(this,"Another place in the same template is already shared under that name", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}else{
			String newName = nameTextField.getText();
			String oldName = place.underlyingPlace().name();
			if(context.activeModel().isNameUsed(newName) && !oldName.equals(newName)){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this, "The specified name is already used by another place or transition.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			try{ // set name
				place.underlyingPlace().setName(newName);
//				Iterable<TAPNQuery> queries = context.queries();
//
//				RenamePlaceTCTLVisitor renameVisitor = new RenamePlaceTCTLVisitor(oldName, newName);
//				for (TAPNQuery q : queries) {
//					q.getProperty().accept(renameVisitor, null);
//				}
//				context.undoManager().addEdit(new RenameTimedPlaceCommand(context.tabContent(), place.underlyingPlace(), oldName, newName));
			}catch(RequireException e){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this, "Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			context.nameGenerator().updateIndices(context.activeModel(), newName);
		
			int newMarking = (Integer)markingSpinner.getValue();
			if(newMarking != place.underlyingPlace().numberOfTokens()){
				Command command = new TimedPlaceMarkingEdit(place, newMarking - place.underlyingPlace().numberOfTokens());
				command.redo();
				context.undoManager().addEdit(command);
			}
			
			TimeInvariant newInvariant = constructInvariant();
			TimeInvariant oldInvariant = place.underlyingPlace().invariant();
			if(!newInvariant.equals(oldInvariant)){
				context.undoManager().addEdit(new ChangedInvariantCommand(place.underlyingPlace(), oldInvariant, newInvariant));
				place.underlyingPlace().setInvariant(newInvariant);
			}
		}

		if ((place.getAttributesVisible() && !attributesCheckBox.isSelected()) || (!place.getAttributesVisible() && attributesCheckBox.isSelected())) {
			place.toggleAttributesVisible();
		}
		place.repaint();

		context.network().buildConstraints();
		exit();
	}

	private TimeInvariant constructInvariant() {
		if(normalInvRadioButton.isSelected()){
			if(invariantInf.isSelected()){
				return TimeInvariant.LESS_THAN_INFINITY;
			}else{
				int bound = (Integer)invariantSpinner.getValue();
				boolean nonStrict = "<=".equals(invRelationNormal.getSelectedItem());
				return new TimeInvariant(nonStrict, new IntBound(bound));
			}
		}else{
			boolean nonStrict = "<=".equals(invRelationConstant.getSelectedItem());
			Constant constant = context.network().getConstant((String)invConstantsComboBox.getSelectedItem());
			return new TimeInvariant(nonStrict, new ConstantBound(constant));
		}
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	private javax.swing.JCheckBox attributesCheckBox;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel markingLabel;
	private javax.swing.JSpinner markingSpinner;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JPanel basicPropertiesPanel;
	private javax.swing.JPanel timeInvariantPanel;
	private JPanel invariantGroup;
	private JComboBox invRelationNormal;
	private JComboBox invRelationConstant;
	private JSpinner invariantSpinner;
	private JCheckBox invariantInf;
	private JComboBox invConstantsComboBox;
	private JRadioButton normalInvRadioButton;
	private JRadioButton constantInvRadioButton;
}
