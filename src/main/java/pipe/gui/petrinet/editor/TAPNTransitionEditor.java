package pipe.gui.petrinet.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import dk.aau.cs.model.tapn.*;
import net.tapaal.gui.petrinet.undo.*;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.SwingHelper;
import net.tapaal.swinghelpers.WidthAdjustingComboBox;
import net.tapaal.gui.petrinet.editor.ColoredTransitionGuardPanel;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import net.tapaal.gui.petrinet.Context;
import dk.aau.cs.util.RequireException;
import pipe.gui.swingcomponents.EscapableDialog;

import static net.tapaal.swinghelpers.GridBagHelper.Fill;
import static net.tapaal.swinghelpers.GridBagHelper.Anchor;

public class TAPNTransitionEditor extends JPanel {

	private static final String untimed_preset_warning = "Incoming arcs to urgent transitions must have the interval [0,\u221e).";
	private static final String transport_destination_invariant_warning = "Transport arcs going through urgent transitions cannot have an invariant at the destination.";
	private final TimedTransitionComponent transition;
    private final EscapableDialog dialog;
	private final JRootPane rootPane;
	private final Context context;
	private JScrollPane scrollPane;
	private JPanel mainPanel;

	private boolean doOKChecked = false;

	private final int maxNumberOfTransitionsToShowAtOnce = 20;
	boolean doNewEdit = true;

	public TAPNTransitionEditor(EscapableDialog _dialog, TimedTransitionComponent _transition, Context context) {
		dialog = _dialog;
        rootPane = _dialog.getRootPane();
		transition = _transition;
		this.context = context;
		initComponents();
        hideIrrelevantInformation();
		rootPane.setDefaultButton(okButton);
	}

	private void hideIrrelevantInformation(){
	    if(!transition.isTimed()) {
            urgentCheckBox.setVisible(false);
        }
	    if(!transition.isColored() || !coloredTransitionGuardPanel.showGuardPanel()){
	        coloredTransitionGuardPanel.setVisible(false);
        }
        distributionPanel.setVisible((!urgentCheckBox.isSelected()) && transition.isStochastic());
    }

	private void initComponents() {
        setLayout(new BorderLayout());
        GridBagConstraints gridBagConstraints;
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
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

        weightField = new JTextField();
        infiniteWeight = new JCheckBox("∞");
        useConstantWeight = new JCheckBox("Use constant");
        ArrayList<String> constants = new ArrayList<>();
        for(Constant c : context.network().constants()) {
            constants.add(c.name());
        }
        constantsComboBox = new JComboBox<>(new DefaultComboBoxModel<>(constants.toArray(new String[0])));
        useConstantWeight.setEnabled(constants.size() > 0);
        useConstantWeight.addActionListener(act -> displayWeight(parseWeight()));

        distributionPanel = new JPanel();
        distributionType = new JComboBox<>(new String[]{"constant", "uniform", "exponential", "normal"});
        distributionParam1Label = new JLabel();
        distributionParam2Label = new JLabel();
        distributionParam1Field = new JTextField();
        distributionParam2Field = new JTextField();
        distributionExplanation = new JLabel();
        SwingHelper.setPreferredWidth(distributionParam1Field, 100);
        SwingHelper.setPreferredWidth(distributionParam2Field, 100);

        DocumentListener updateDistribDisplay = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                display();
            }
            public void removeUpdate(DocumentEvent e) {
                display();
            }
            public void insertUpdate(DocumentEvent e) {
                display();
            }
            public void display() {
                SMCDistribution distrib = parseDistribution();
                distributionExplanation.setText(distrib.explanation());
            }
        };
        distributionParam1Field.getDocument().addDocumentListener(updateDistribDisplay);
        distributionParam2Field.getDocument().addDocumentListener(updateDistribDisplay);

		sharedTransitionsComboBox = new WidthAdjustingComboBox<>(maxNumberOfTransitionsToShowAtOnce);
		SwingHelper.setPreferredWidth(sharedTransitionsComboBox,290);
		sharedTransitionsComboBox.addActionListener(e -> {
		    //coloredTransitionGuardPanel.onOK(context.undoManager());
			if(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).transitions().isEmpty()){
                ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUrgent(urgentCheckBox.isSelected());
                ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUncontrollable(uncontrollableCheckBox.isSelected());
                ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setGuard(coloredTransitionGuardPanel.getExpression());
                ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setDistribution(parseDistribution());
                ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setWeight(parseWeight());
            }else{
                urgentCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUrgent());
                uncontrollableCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUncontrollable());
                coloredTransitionGuardPanel.initExpr(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).getGuard());
                displayDistributionFields(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).getDistribution());
                displayWeight(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).getWeight());
            }
		});

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
                context.nameGenerator().getNewTransitionName(context.activeModel()) : transition.getName());
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
        if(transition.isStochastic()) {
            urgentCheckBox.setToolTipText("Note: for SMC, it is recommended to prefer setting a constant(0) distribution instead of using urgent transitions");
        }
		
		urgentCheckBox.addActionListener(e -> {
			if(!isUrgencyOK()){
				urgentCheckBox.setSelected(false);
			}
            distributionPanel.setVisible((!urgentCheckBox.isSelected()) && transition.isStochastic());
            dialog.pack();
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        transitionEditorPanel.add(useConstantWeight, gridBagConstraints);
        gridBagConstraints.gridy = 4;
        if(context.tabContent().getLens().isStochastic()) {
            String weightToolTip = "Probability mass of the transition in the event of a firing date collision";
            JLabel weightLabel = new JLabel("Weight :");
            weightLabel.setToolTipText(weightToolTip);
            transitionEditorPanel.add(weightLabel, gridBagConstraints);
            gridBagConstraints.gridx = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            transitionEditorPanel.add(weightField, gridBagConstraints);
            transitionEditorPanel.add(constantsComboBox, gridBagConstraints);
            weightField.setToolTipText(weightToolTip);
            gridBagConstraints.gridx = 2;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            transitionEditorPanel.add(infiniteWeight, gridBagConstraints);
            infiniteWeight.addActionListener(act -> weightField.setEnabled(!infiniteWeight.isSelected()));
        }

        distributionType.addActionListener(actionEvent -> {
            if(!distributionType.hasFocus()) return;
            switch (String.valueOf(distributionType.getSelectedItem())) {
                case SMCConstantDistribution.NAME:
                    displayDistributionFields(SMCConstantDistribution.defaultDistribution());
                    break;
                case SMCUniformDistribution.NAME:
                    displayDistributionFields(SMCUniformDistribution.defaultDistribution());
                    break;
                case SMCExponentialDistribution.NAME:
                    displayDistributionFields(SMCExponentialDistribution.defaultDistribution());
                    break;
                case SMCNormalDistribution.NAME:
                    displayDistributionFields(SMCNormalDistribution.defaultDistribution());
                    break;
            }
        });
        distributionPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = GridBagHelper.as(0,0, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        distributionPanel.add(new JLabel("Distribution :"), gbc);
        gbc = GridBagHelper.as(1,0, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        distributionPanel.add(distributionType, gbc);
        gbc = GridBagHelper.as(0,1, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        distributionPanel.add(distributionParam1Label, gbc);
        gbc = GridBagHelper.as(1,1, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        distributionPanel.add(distributionParam1Field, gbc);
        gbc = GridBagHelper.as(0,2, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        distributionPanel.add(distributionParam2Label, gbc);
        gbc = GridBagHelper.as(1,2, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        distributionPanel.add(distributionParam2Field, gbc);
        gbc = GridBagHelper.as(0,3, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        gbc.gridwidth = 2;
        distributionPanel.add(distributionExplanation, gbc);
        gridBagConstraints = GridBagHelper.as(0, 5, Fill.HORIZONTAL, new Insets(3, 3, 3, 3));
        gridBagConstraints.gridwidth = 4;
        transitionEditorPanel.add(distributionPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		mainPanel.add(transitionEditorPanel, gridBagConstraints);

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
		mainPanel.add(buttonPanel, gridBagConstraints);

		attributesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		attributesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		gridBagConstraints = GridBagHelper.as(1,transition.isStochastic() ? 6 : 3, Anchor.WEST, new Insets(3, 3, 3, 3));
		transitionEditorPanel.add(attributesCheckBox, gridBagConstraints);

		gridBagConstraints = GridBagHelper.as(0,1,Anchor.WEST, new Insets(3, 3, 3, 3));
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
        coloredTransitionGuardPanel = new ColoredTransitionGuardPanel(transition, context, this);
		mainPanel.add(coloredTransitionGuardPanel, gridBagConstraints);
		setupInitialState();
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(mainPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.CENTER);
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
		coloredTransitionGuardPanel.initExpr(transition.getGuardExpression());

        if(context.tabContent().getLens().isStochastic()) {
            displayDistribution();
            displayWeight();
        }

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
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		urgentCheckBox.setSelected(transition.isUrgent());
		uncontrollableCheckBox.setSelected(transition.isUncontrollable());
        displayDistributionFields(transition.underlyingTransition().getDistribution());
        displayWeight(transition.underlyingTransition().getWeight());
        uncontrollableCheckBox.setSelected(transition.isUncontrollable());
		transitionEditorPanel.add(nameTextField, gbc);
		transitionEditorPanel.validate();
		transitionEditorPanel.repaint();
	}

	protected void switchToNameDropDown() {
		transitionEditorPanel.remove(nameTextField);
		GridBagConstraints gbc = GridBagHelper.as(1,1,Fill.HORIZONTAL,new Insets(3, 3, 3, 3));
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
		transitionEditorPanel.add(sharedTransitionsComboBox, gbc);
		if(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).transitions().isEmpty()){
            ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUrgent(urgentCheckBox.isSelected());
            ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setUncontrollable(uncontrollableCheckBox.isSelected());
            ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setGuard(coloredTransitionGuardPanel.getExpression());
            ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setDistribution(parseDistribution());
            ((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).setWeight(parseWeight());
        }else{
            urgentCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUrgent());
            uncontrollableCheckBox.setSelected(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).isUncontrollable());
            coloredTransitionGuardPanel.initExpr(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).getGuard());
            displayDistributionFields(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).getDistribution());
            displayWeight(((SharedTransition)sharedTransitionsComboBox.getSelectedItem()).getWeight());
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
			if(transition.underlyingTransition().model().isNameUsed(newName) && (wasShared || !transition.underlyingTransition().name().equals(newName))){
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
						JOptionPane.showMessageDialog(this, "A place with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				transition.setUrgent(urgentCheckBox.isSelected());
				transition.setUncontrollable(uncontrollableCheckBox.isSelected());
                transition.underlyingTransition().setDistribution(parseDistribution());
                transition.underlyingTransition().setWeight(parseWeight());
			}
		}
		
		if(transition.isUrgent() != urgentCheckBox.isSelected()){
			context.undoManager().addEdit(new ToggleTransitionUrgentCommand(transition.underlyingTransition(), context.tabContent()));
			transition.setUrgent(urgentCheckBox.isSelected());
		}
        if(transition.isUncontrollable() != uncontrollableCheckBox.isSelected()){
            context.undoManager().addEdit(new ToggleTransitionUncontrollableCommand(transition.underlyingTransition(), context.tabContent()));
            transition.setUncontrollable(uncontrollableCheckBox.isSelected());
        }

        SMCDistribution distribution = parseDistribution();
        if(!transition.underlyingTransition().getDistribution().equals(distribution)) {
            context.undoManager().addEdit(new ChangeTransitionDistributionCommand(transition.underlyingTransition(), context.tabContent(), distribution));
            transition.underlyingTransition().setDistribution(distribution);
        }

        Probability weight = parseWeight();
        //TODO : Undo weight
        transition.underlyingTransition().setWeight(weight);

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

		coloredTransitionGuardPanel.onOK(context.undoManager());
		doOKChecked = true;

		return true;
	}

	public void enableOKButton(boolean enable){
	    okButton.setEnabled(enable);
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

    private Probability parseWeight() {
        if(useConstantWeight.isSelected()) {
            Constant constant = context.network().getConstant((String) constantsComboBox.getSelectedItem());
            return new ConstantProbability(constant);
        }
        if(infiniteWeight.isSelected()) {
            return new DoubleProbability(Double.POSITIVE_INFINITY);
        }
        try {
            return new DoubleProbability(Double.parseDouble(weightField.getText()));
        } catch(NumberFormatException e) {
            return new DoubleProbability(1.0);
        }
    }

    private void displayWeight() {
        displayWeight(transition.underlyingTransition().getWeight());
    }

    private void displayWeight(Probability weight) {
        if(weight instanceof ConstantProbability) {
            displayConstantWeight(weight);
        } else {
            displayDoubleWeight(weight);
        }
    }

    private void displayConstantWeight(Probability weight) {
        weightField.setVisible(false);
        infiniteWeight.setVisible(false);
        constantsComboBox.setVisible(true);
        useConstantWeight.setSelected(true);
        ConstantProbability constWeight = (ConstantProbability) weight;
        constantsComboBox.setSelectedItem(constWeight.constant().name());
    }

    private void displayDoubleWeight(Probability weight) {
        weightField.setVisible(true);
        infiniteWeight.setVisible(true);
        constantsComboBox.setVisible(false);
        useConstantWeight.setSelected(false);
        if(Double.isInfinite(weight.value())) {
            infiniteWeight.setSelected(true);
        } else {
            weightField.setText(String.valueOf(weight.value()));
        }
        weightField.setEnabled(!infiniteWeight.isSelected());
    }

    private SMCDistribution parseDistribution() {
        if(transition.isUrgent()) {
            return SMCDistribution.urgent();
        }
        String type = String.valueOf(distributionType.getSelectedItem());
        try {
            switch (type) {
                case SMCConstantDistribution.NAME:
                    double value = Double.parseDouble(distributionParam1Field.getText());
                    return new SMCConstantDistribution(value);
                case SMCUniformDistribution.NAME:
                    double a = Double.parseDouble(distributionParam1Field.getText());
                    double b = Double.parseDouble(distributionParam2Field.getText());
                    return new SMCUniformDistribution(a, b);
                case SMCExponentialDistribution.NAME:
                    double rate = Double.parseDouble(distributionParam1Field.getText());
                    return new SMCExponentialDistribution(rate);
                case SMCNormalDistribution.NAME:
                    double mean = Double.parseDouble(distributionParam1Field.getText());
                    double stddev = Double.parseDouble(distributionParam2Field.getText());
                    return new SMCNormalDistribution(mean, stddev);
            }
        } catch(NumberFormatException ignored) {}
        switch (type) {
            case SMCConstantDistribution.NAME:
                return SMCConstantDistribution.defaultDistribution();
            case SMCUniformDistribution.NAME:
                return SMCUniformDistribution.defaultDistribution();
            case SMCExponentialDistribution.NAME:
                return SMCExponentialDistribution.defaultDistribution();
            case SMCNormalDistribution.NAME:
                return SMCNormalDistribution.defaultDistribution();
            default:
                return SMCDistribution.defaultDistribution();
        }
    }

    private void displayDistribution() {
        SMCDistribution distribution = transition.underlyingTransition().getDistribution();
        distributionType.setSelectedItem(distribution.distributionName());
        displayDistributionFields(distribution);
    }

    private void displayDistributionFields(SMCDistribution distribution) {
        switch (distribution.distributionName()) {
            case SMCConstantDistribution.NAME:
                distributionParam1Label.setText("Value :");
                distributionParam1Field.setText(String.valueOf(((SMCConstantDistribution) distribution).value));
                distributionParam2Label.setVisible(false);
                distributionParam2Field.setVisible(false);
                break;
            case SMCUniformDistribution.NAME:
                distributionParam1Label.setText("A :");
                distributionParam2Label.setText("B :");
                distributionParam1Field.setText(String.valueOf(((SMCUniformDistribution) distribution).a));
                distributionParam2Field.setText(String.valueOf(((SMCUniformDistribution) distribution).b));
                distributionParam2Label.setVisible(true);
                distributionParam2Field.setVisible(true);
                break;
            case SMCExponentialDistribution.NAME:
                distributionParam1Label.setText("Rate :");
                distributionParam1Field.setText(String.valueOf(((SMCExponentialDistribution) distribution).rate));
                distributionParam2Label.setVisible(false);
                distributionParam2Field.setVisible(false);
                break;
            case SMCNormalDistribution.NAME:
                distributionParam1Label.setText("Mean :");
                distributionParam2Label.setText("Std. Dev. :");
                distributionParam1Field.setText(String.valueOf(((SMCNormalDistribution) distribution).mean));
                distributionParam2Field.setText(String.valueOf(((SMCNormalDistribution) distribution).stddev));
                distributionParam2Label.setVisible(true);
                distributionParam2Field.setVisible(true);
                break;
            default:
                break;
        }
        distributionExplanation.setText(distribution.explanation());
        distributionType.setFocusable(false);
        distributionType.setSelectedItem(distribution.distributionName());
        distributionType.setFocusable(true);
        dialog.pack();
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

    private JTextField weightField;
    private JCheckBox infiniteWeight;
    private JCheckBox useConstantWeight;
    private JComboBox<String> constantsComboBox;

    private JPanel distributionPanel;
    private JComboBox<String> distributionType;
    private JLabel distributionParam1Label;
    private JLabel distributionParam2Label;
    private JTextField distributionParam1Field;
    private JTextField distributionParam2Field;
    private JLabel distributionExplanation;

    private javax.swing.JCheckBox uncontrollableCheckBox;

	private boolean makeNewShared = false;
	private javax.swing.JCheckBox attributesCheckBox;
    private ColoredTransitionGuardPanel coloredTransitionGuardPanel;
}
