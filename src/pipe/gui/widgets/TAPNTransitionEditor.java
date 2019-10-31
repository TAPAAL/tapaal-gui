package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.CaretListener;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MakePlaceNewSharedMultiCommand;
import dk.aau.cs.gui.undo.MakeTransitionNewSharedCommand;
import dk.aau.cs.gui.undo.MakeTransitionNewSharedMultiCommand;
import dk.aau.cs.gui.undo.MakeTransitionSharedCommand;
import dk.aau.cs.gui.undo.RenameTimedTransitionCommand;
import dk.aau.cs.gui.undo.ToggleTransitionUrgent;
import dk.aau.cs.gui.undo.UnshareTransitionCommand;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.RequireException;

public class TAPNTransitionEditor extends javax.swing.JPanel {
	private static final long serialVersionUID = 1744651413834659994L;
	private static final String untimed_preset_warning = "Incoming arcs to urgent transitions must have the interval [0,inf).";
	private static final String transport_destination_invariant_warning = "Transport arcs going through urgent transitions cannot have an invariant at the destination.";
	private TimedTransitionComponent transition;
	private JRootPane rootPane;
	private Context context;
	
	private int maxNumberOfTransitionsToShowAtOnce = 20;
	boolean doNewEdit = true;

	public TAPNTransitionEditor(JRootPane _rootPane, TimedTransitionComponent _transition, Context context) {
		rootPane = _rootPane;
		transition = _transition;
		this.context = context;
		initComponents();

		rootPane.setDefaultButton(okButton);
	}

	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		transitionEditorPanel = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel();
		nameTextField = new javax.swing.JTextField();
		nameTextField.setPreferredSize(new Dimension(290,27));
		rotationLabel = new javax.swing.JLabel();
		rotationComboBox = new javax.swing.JComboBox();
		buttonPanel = new javax.swing.JPanel();
		cancelButton = new javax.swing.JButton();
		makeSharedButton = new javax.swing.JButton();
		okButton = new javax.swing.JButton();
		sharedCheckBox = new JCheckBox("Shared");
		urgentCheckBox = new JCheckBox("Urgent");
		attributesCheckBox = new JCheckBox("Show transition name");
		
		
		
		
		sharedTransitionsComboBox = new WidthAdjustingComboBox(maxNumberOfTransitionsToShowAtOnce);
		sharedTransitionsComboBox.setPreferredSize(new Dimension(290,27));
		sharedTransitionsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).transitions().isEmpty()){
					((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUrgent(urgentCheckBox.isSelected());
				}else{
					urgentCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUrgent());
				}
			}
		});

		setLayout(new java.awt.GridBagLayout());

		transitionEditorPanel.setLayout(new java.awt.GridBagLayout());
		transitionEditorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transition Editor"));

		sharedCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox box = (JCheckBox)arg0.getSource();
				if(box.isSelected()){
					switchToNameDropDown();
					makeSharedButton.setEnabled(false);
				}else{
					switchToNameTextField();
                                        nameTextField.setText(transition.underlyingTransition().isShared()? 
                                                CreateGui.getDrawingSurface().getNameGenerator().getNewTransitionName(context.activeModel()) : transition.getName());
					makeSharedButton.setEnabled(true);
				}
			}		
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(sharedCheckBox, gridBagConstraints);	
		
		
		makeSharedButton = new javax.swing.JButton();
		makeSharedButton.setText("Make shared");
		makeSharedButton.setMaximumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setMinimumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setPreferredSize(new java.awt.Dimension(110, 25));
		
		makeSharedButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				makeNewShared = true;
				if(okButtonHandler(evt)){
					
					makeSharedButton.setEnabled(false);
					sharedCheckBox.setEnabled(true);
					sharedCheckBox.setSelected(true);
					setupInitialState();
				}
				makeNewShared = false;
			}
		});
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		transitionEditorPanel.add(makeSharedButton, gridBagConstraints);
		
		nameLabel.setText("Name:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(nameLabel, gridBagConstraints);

		nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				nameTextFieldFocusGained(evt);
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				nameTextFieldFocusLost(evt);
			}
		});
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(urgentCheckBox, gridBagConstraints);
		
		urgentCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!isUrgencyOK()){
					urgentCheckBox.setSelected(false);
				}
			}
		});
	
		rotationLabel.setText("Rotate:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(rotationLabel, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(rotationComboBox, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		add(transitionEditorPanel, gridBagConstraints);

		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(okButtonHandler(evt)){
					exit();
				}
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonHandler(evt);
			}
		});
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		buttonPanel.add(cancelButton, gridBagConstraints);

		

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		buttonPanel.add(okButton, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 8, 3);
		add(buttonPanel, gridBagConstraints);

		attributesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		attributesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(attributesCheckBox, gridBagConstraints);
		
		setupInitialState();

	}	
	
	private void setupInitialState(){
		sharedTransitions = new Vector<SharedTransition>(context.network().sharedTransitions());
		ArrayList<SharedTransition> usedTransitions = new ArrayList<SharedTransition>();
		
		for (TimedTransition tt : context.activeModel().transitions()){
			if(tt.isShared()){
				usedTransitions.add(tt.sharedTransition());
			}
		}
		
		sharedTransitions.removeAll(usedTransitions);
		if (transition.underlyingTransition().isShared()){
			sharedTransitions.add(transition.underlyingTransition().sharedTransition());
		}
		
		Collections.sort(sharedTransitions, new Comparator<SharedTransition>() {
			public int compare(SharedTransition o1, SharedTransition o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});
		
		rotationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"0\u00B0", "+45\u00B0", "+90\u00B0", "-45\u00B0" }));
		nameTextField.setText(transition.getName());
		sharedTransitionsComboBox.setModel(new DefaultComboBoxModel(sharedTransitions));
		sharedCheckBox.setEnabled(sharedTransitions.size() > 0 && !hasArcsToSharedPlaces(transition.underlyingTransition()));
		urgentCheckBox.setSelected(transition.isUrgent());
		
		if(transition.underlyingTransition().isShared()){
			switchToNameDropDown();
			sharedCheckBox.setSelected(true);
			sharedTransitionsComboBox.setSelectedItem(transition.underlyingTransition().sharedTransition());
		}else{
			switchToNameTextField();
		}
		makeSharedButton.setEnabled(!sharedCheckBox.isSelected() && !hasArcsToSharedPlaces(transition.underlyingTransition()));
		attributesCheckBox.setSelected(transition.getAttributesVisible());
	}
	
	private boolean hasArcsToSharedPlaces(TimedTransition underlyingTransition) {
		for(TimedInputArc arc : context.activeModel().inputArcs()){
			if(arc.destination().equals(underlyingTransition) && arc.source().isShared()) return true;
		}
		
		for(TimedOutputArc arc : context.activeModel().outputArcs()){
			if(arc.source().equals(underlyingTransition) && arc.destination().isShared()) return true;
		}
		
		for(TransportArc arc : context.activeModel().transportArcs()){
			if(arc.transition().equals(underlyingTransition) && arc.source().isShared()) return true;
			if(arc.transition().equals(underlyingTransition) && arc.destination().isShared()) return true;
		}
		
		for(TimedInhibitorArc arc : context.activeModel().inhibitorArcs()){
			if(arc.destination().equals(underlyingTransition) && arc.source().isShared()) return true;
		}
		
		return false;
	}

	protected void switchToNameTextField() {
		transitionEditorPanel.remove(sharedTransitionsComboBox);
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		urgentCheckBox.setSelected(transition.isUrgent());
		transitionEditorPanel.add(nameTextField, gbc);	
		transitionEditorPanel.validate();
		transitionEditorPanel.repaint();
	}

	protected void switchToNameDropDown() {
		transitionEditorPanel.remove(nameTextField);
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(sharedTransitionsComboBox, gbc);		
		if(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).transitions().isEmpty()){
			((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUrgent(urgentCheckBox.isSelected());
		}else{
			urgentCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUrgent());
		}
		transitionEditorPanel.validate();
		transitionEditorPanel.repaint();
	}

	private void nameTextFieldFocusLost(java.awt.event.FocusEvent evt) {
		focusLost(nameTextField);
	}
	
	private void nameTextFieldFocusGained(java.awt.event.FocusEvent evt) {
		focusGained(nameTextField);
	}

	private void focusGained(javax.swing.JTextField textField) {
		textField.setCaretPosition(0);
		textField.moveCaretPosition(textField.getText().length());
	}

	private void focusLost(javax.swing.JTextField textField) {
		textField.setCaretPosition(0);
	}

	CaretListener caretListener = new javax.swing.event.CaretListener() {
		public void caretUpdate(javax.swing.event.CaretEvent evt) {
			JTextField textField = (JTextField) evt.getSource();
			textField.setBackground(new Color(255, 255, 255));
			// textField.removeChangeListener(this);
		}
	};
	
	private boolean isUrgencyOK(){
		if(!transition.hasUntimedPreset()){
			JOptionPane.showMessageDialog(transitionEditorPanel, untimed_preset_warning, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		for(TransportArc arc : transition.underlyingTransition().getTransportArcsGoingThrough()){
			if(arc.destination().invariant().upperBound() != Bound.Infinity){
				JOptionPane.showMessageDialog(transitionEditorPanel, transport_destination_invariant_warning, "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	private boolean okButtonHandler(java.awt.event.ActionEvent evt) {
		String newName = nameTextField.getText();
		
		// Check urgent constrain
		if(urgentCheckBox.isSelected() && !isUrgencyOK()){
			return false;
		}
		//Only do new edit if it has not already been done
		if(doNewEdit) {
			context.undoManager().newEdit(); // new "transaction""
			doNewEdit = false;
		}
		
		boolean wasShared = transition.underlyingTransition().isShared() && !sharedCheckBox.isSelected();
		if(transition.underlyingTransition().isShared()){
			context.undoManager().addEdit(new UnshareTransitionCommand(transition.underlyingTransition().sharedTransition(), transition.underlyingTransition()));
			transition.underlyingTransition().unshare();
		}
		
		if(sharedCheckBox.isSelected()){
			SharedTransition selectedTransition = (SharedTransition)sharedTransitionsComboBox.getSelectedItem();
            Command command = new MakeTransitionSharedCommand(context.activeModel(), selectedTransition, transition.underlyingTransition(), context.tabContent());
			context.undoManager().addEdit(command);
			try{
				command.redo();
			}catch(RequireException e){
				context.undoManager().undo();
				JOptionPane.showMessageDialog(this,"Another transition in the same component is already shared under that name", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}else{		
			if(transition.underlyingTransition().model().isNameUsed(newName) && (wasShared || !transition.underlyingTransition().name().equalsIgnoreCase(newName))){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this,
						"The specified name is already used by another place or transition.",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			try{
				String oldName = transition.underlyingTransition().name();
				transition.underlyingTransition().setName(newName);
				Command renameCommand = new RenameTimedTransitionCommand(context.tabContent(), transition.underlyingTransition(), oldName, newName);
				context.undoManager().addEdit(renameCommand);
				// set name
				renameCommand.redo();
			}catch(RequireException e){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this,
						"Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			context.nameGenerator().updateIndices(transition.underlyingTransition().model(), newName);
		
			
			if(makeNewShared){
				Command command = new MakeTransitionNewSharedCommand(context.activeModel(), newName, transition.underlyingTransition(), context.tabContent(), false);
				context.undoManager().addEdit(command);
				try{
					command.redo();
				}catch(RequireException e){
					context.undoManager().undo();
					//This is checked as a transition cannot be shared if there exists a place with the same name
					if(transition.underlyingTransition().model().parentNetwork().isNameUsedForTransitionsOnly(newName)) {
						int dialogResult = JOptionPane.showConfirmDialog(this, "A transition with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords. \n\nThis transition name will be changed into shared one also in all other components.", "Error", JOptionPane.OK_CANCEL_OPTION);
						if(dialogResult == JOptionPane.OK_OPTION) {
							Command cmd = new MakeTransitionNewSharedMultiCommand(context, newName, transition);	
							cmd.redo();
							context.undoManager().addEdit(cmd);
						} else {
							return false;
						}
					} else {
						JOptionPane.showMessageDialog(this, "A place with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.OK_OPTION);
						return false;
					}
				}
				transition.setUrgent(urgentCheckBox.isSelected());
			}  
		}
		
		if(transition.isUrgent() != urgentCheckBox.isSelected()){
			context.undoManager().addEdit(new ToggleTransitionUrgent(transition.underlyingTransition()));
			transition.setUrgent(urgentCheckBox.isSelected());
		}

		Integer rotationIndex = rotationComboBox.getSelectedIndex();
		if (rotationIndex > 0) {
			int angle = 0;
			switch (rotationIndex) {
			case 1:
				angle = 45;
				break;
			case 2:
				angle = 90;
				break;
			case 3:
				angle = 135; // -45
				break;
			default:
				break;
			}
			if (angle != 0) {
				context.undoManager().addEdit(transition.rotate(angle));
			}
		}
		
		if(transition.getAttributesVisible() && !attributesCheckBox.isSelected() || (!transition.getAttributesVisible() && attributesCheckBox.isSelected())) {
			transition.toggleAttributesVisible();
		}
		
		transition.update(true);
		
		return true;
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	private void cancelButtonHandler(java.awt.event.ActionEvent evt) {
		exit();
	}

	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JButton makeSharedButton;
	private javax.swing.JComboBox rotationComboBox;
	private javax.swing.JLabel rotationLabel;
	private javax.swing.JPanel transitionEditorPanel;
	private javax.swing.JCheckBox sharedCheckBox;
	private javax.swing.JComboBox sharedTransitionsComboBox;
	private javax.swing.JCheckBox urgentCheckBox;
	private javax.swing.JCheckBox makeNewSharedCheckBox;
	private Vector<SharedTransition> sharedTransitions;
	private boolean makeNewShared = false;
	private javax.swing.JCheckBox attributesCheckBox;
}
