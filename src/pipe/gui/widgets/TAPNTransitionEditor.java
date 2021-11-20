package pipe.gui.widgets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.CaretListener;
import net.tapaal.swinghelpers.GridBagHelper;
import dk.aau.cs.gui.undo.*;
import net.tapaal.swinghelpers.SwingHelper;
import net.tapaal.swinghelpers.WidthAdjustingComboBox;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.tapn.TimedTransitionComponent;
import dk.aau.cs.gui.Context;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.RequireException;

import static net.tapaal.swinghelpers.GridBagHelper.Fill;
import static net.tapaal.swinghelpers.GridBagHelper.Anchor;

public class TAPNTransitionEditor extends JPanel {

	private static final String untimed_preset_warning = "Incoming arcs to urgent transitions must have the interval [0,\u221e).";
	private static final String transport_destination_invariant_warning = "Transport arcs going through urgent transitions cannot have an invariant at the destination.";
	private final TimedTransitionComponent transition;
	private final JRootPane rootPane;
	private final Context context;

	private boolean doOKChecked = false;
	
	private final int maxNumberOfTransitionsToShowAtOnce = 20;
	boolean doNewEdit = true;

	public TAPNTransitionEditor(JRootPane _rootPane, TimedTransitionComponent _transition, Context context) {
		rootPane = _rootPane;
		transition = _transition;
		this.context = context;
		initComponents();
        hideTimedInformation();
		rootPane.setDefaultButton(okButton);
	}

	private void hideTimedInformation(){
	    if(!transition.isTimed()) {
            urgentCheckBox.setVisible(false);
        }
    }

	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		transitionEditorPanel = new JPanel();
		nameLabel = new JLabel();
		nameTextField = new JTextField();
        SwingHelper.setPreferredWidth(nameTextField, 290);
		rotationLabel = new JLabel();
		rotationComboBox = new JComboBox<>();
		buttonPanel = new JPanel();
		cancelButton = new JButton();
		makeSharedButton = new JButton();
		okButton = new JButton();
		sharedCheckBox = new JCheckBox("Shared");
		urgentCheckBox = new JCheckBox("Urgent");
		uncontrollableCheckBox = new JCheckBox("Uncontrollable");
		attributesCheckBox = new JCheckBox("Show transition name");


		sharedTransitionsComboBox = new WidthAdjustingComboBox<>(maxNumberOfTransitionsToShowAtOnce);
		SwingHelper.setPreferredWidth(sharedTransitionsComboBox,290);
		sharedTransitionsComboBox.addActionListener(e -> {
			if(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).transitions().isEmpty()){
                ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUrgent(urgentCheckBox.isSelected());
                ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUncontrollable(uncontrollableCheckBox.isSelected());
			}else{
                urgentCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUrgent());
                uncontrollableCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUncontrollable());
			}
		});

		setLayout(new java.awt.GridBagLayout());

		transitionEditorPanel.setLayout(new java.awt.GridBagLayout());
		transitionEditorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transition Editor"));

		sharedCheckBox.addActionListener(arg0 -> {
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
        });
		gridBagConstraints = GridBagHelper.as(2, 1, Anchor.WEST, new Insets(3, 3, 3, 3));
		transitionEditorPanel.add(sharedCheckBox, gridBagConstraints);	
		
		
		makeSharedButton = new JButton();
		makeSharedButton.setText("Make shared");
		makeSharedButton.setMaximumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setMinimumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setPreferredSize(new java.awt.Dimension(110, 25));
		
		makeSharedButton.addActionListener(evt -> {
            makeNewShared = true;
            makeSharedButton.setEnabled(false);
            if(okButtonHandler(evt)){
                sharedCheckBox.setEnabled(true);
                sharedCheckBox.setSelected(true);
                setupInitialState();
            } else {
                makeSharedButton.setEnabled(true);
                doOKChecked = false;
            }
        });
		
		gridBagConstraints = GridBagHelper.as(3,1, Anchor.WEST, new Insets(5, 5, 5, 5));
		transitionEditorPanel.add(makeSharedButton, gridBagConstraints);
		
		nameLabel.setText("Name:");
		gridBagConstraints = GridBagHelper.as(0,1, Anchor.EAST, new Insets(3, 3, 3, 3));
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
		
		gridBagConstraints = GridBagHelper.as(2, 2, Anchor.WEST, new Insets(3, 3, 3, 3));
		transitionEditorPanel.add(urgentCheckBox, gridBagConstraints);
		
		urgentCheckBox.addActionListener(e -> {
			if(!isUrgencyOK()){
				urgentCheckBox.setSelected(false);
			}
		});

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        if (context.tabContent().getLens().isGame()) {
            transitionEditorPanel.add(uncontrollableCheckBox, gridBagConstraints);

            uncontrollableCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    JCheckBox box = (JCheckBox) arg0.getSource();
                    uncontrollableCheckBox.setSelected(box.isSelected());
                }
            });
        }
	
		rotationLabel.setText("Rotate:");
		gridBagConstraints = GridBagHelper.as(0,2, Anchor.NORTH, new Insets(3, 3, 3, 3));
		transitionEditorPanel.add(rotationLabel, gridBagConstraints);

		gridBagConstraints = GridBagHelper.as(1,2, Anchor.NORTHWEST, new Insets(3, 3, 3, 3));
		transitionEditorPanel.add(rotationComboBox, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		add(transitionEditorPanel, gridBagConstraints);

		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.addActionListener(evt -> {
			if(okButtonHandler(evt)){
				exit();
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.addActionListener(this::cancelButtonHandler);
		
		gridBagConstraints = GridBagHelper.as(0,1, Anchor.EAST, new Insets(3, 3, 3, 3));
		buttonPanel.add(cancelButton, gridBagConstraints);

		

		gridBagConstraints = GridBagHelper.as(1,1, Anchor.WEST, new Insets(3, 3, 3, 3));
		buttonPanel.add(okButton, gridBagConstraints);

		gridBagConstraints = GridBagHelper.as(0,3, Anchor.EAST, new Insets(5, 0, 8, 3));
		add(buttonPanel, gridBagConstraints);

		attributesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		attributesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		gridBagConstraints = GridBagHelper.as(1,3, Anchor.WEST, new Insets(3, 3, 3, 3));
		transitionEditorPanel.add(attributesCheckBox, gridBagConstraints);
		
		setupInitialState();

	}	
	
	private void setupInitialState(){
		sharedTransitions = new Vector<>(context.network().sharedTransitions());
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
		
		sharedTransitions.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));
		
		rotationComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {"0\u00B0", "+45\u00B0", "+90\u00B0", "-45\u00B0" }));
		nameTextField.setText(transition.getName());
		sharedTransitionsComboBox.setModel(new DefaultComboBoxModel<>(sharedTransitions));
		sharedCheckBox.setEnabled(sharedTransitions.size() > 0 && !hasArcsToSharedPlaces(transition.underlyingTransition()));
		urgentCheckBox.setSelected(transition.isUrgent());
		uncontrollableCheckBox.setSelected(transition.isUncontrollable());

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
		GridBagConstraints gbc = GridBagHelper.as(1,1, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
		urgentCheckBox.setSelected(transition.isUrgent());
		uncontrollableCheckBox.setSelected(transition.isUncontrollable());
		transitionEditorPanel.add(nameTextField, gbc);
		transitionEditorPanel.validate();
		transitionEditorPanel.repaint();
	}

	protected void switchToNameDropDown() {
		transitionEditorPanel.remove(nameTextField);
		GridBagConstraints gbc = GridBagHelper.as(1,1,Fill.HORIZONTAL,new Insets(3, 3, 3, 3));

		transitionEditorPanel.add(sharedTransitionsComboBox, gbc);
		if(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).transitions().isEmpty()){
            ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUrgent(urgentCheckBox.isSelected());
            ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUncontrollable(uncontrollableCheckBox.isSelected());
        }else{
            urgentCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUrgent());
            uncontrollableCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUncontrollable());
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

	private void focusGained(JTextField textField) {
		textField.setCaretPosition(0);
		textField.moveCaretPosition(textField.getText().length());
	}

	private void focusLost(JTextField textField) {
		textField.setCaretPosition(0);
	}

	CaretListener caretListener = evt -> {
		JTextField textField = (JTextField) evt.getSource();
		textField.setBackground(new Color(255, 255, 255));
		// textField.removeChangeListener(this);
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
				if (!oldName.equals(newName)) {
				transition.underlyingTransition().setName(newName);
				Command renameCommand = new RenameTimedTransitionCommand(context.tabContent(), transition.underlyingTransition(), oldName, newName);
				context.undoManager().addEdit(renameCommand);
				// set name
				renameCommand.redo();
				}
			}catch(RequireException e){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this,
						"Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			context.nameGenerator().updateIndices(transition.underlyingTransition().model(), newName);
		
			
			if(makeNewShared && !makeSharedButton.isEnabled()){
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
						JOptionPane.showMessageDialog(this, "A place with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				transition.setUrgent(urgentCheckBox.isSelected());
				transition.setUncontrollable(uncontrollableCheckBox.isSelected());
			}
		}
		
		if(transition.isUrgent() != urgentCheckBox.isSelected()){
			context.undoManager().addEdit(new ToggleTransitionUrgent(transition.underlyingTransition(), context.tabContent()));
			transition.setUrgent(urgentCheckBox.isSelected());
		}
        if(transition.isUncontrollable() != uncontrollableCheckBox.isSelected()){
            context.undoManager().addEdit(new ToggleTransitionUncontrollable(transition.underlyingTransition(), context.tabContent()));
            transition.setUncontrollable(uncontrollableCheckBox.isSelected());
        }

		int rotationIndex = rotationComboBox.getSelectedIndex();
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

            Map<PetriNetObject, Boolean> map = new HashMap<>();
            map.put(transition, !transition.getAttributesVisible());

            Command changeVisibility = new ChangeAllNamesVisibilityCommand(context.tabContent(), null, map, transition.getAttributesVisible());
            context.undoManager().addEdit(changeVisibility);
		}
		
		transition.update(true);

		doOKChecked = true;
		
		return true;
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	private void cancelButtonHandler(java.awt.event.ActionEvent evt) {
        if (doOKChecked) {
            context.undoManager().undo();
        }
		exit();
	}

	private JPanel buttonPanel;
	private JButton cancelButton;
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JButton okButton;
	private JButton makeSharedButton;
	private javax.swing.JComboBox<String> rotationComboBox;
	private JLabel rotationLabel;
	private JPanel transitionEditorPanel;
	private javax.swing.JCheckBox sharedCheckBox;
	private javax.swing.JComboBox<SharedTransition> sharedTransitionsComboBox;

	private javax.swing.JCheckBox urgentCheckBox;
    private Vector<SharedTransition> sharedTransitions;

    private javax.swing.JCheckBox uncontrollableCheckBox;

	private boolean makeNewShared = false;
	private javax.swing.JCheckBox attributesCheckBox;
}
