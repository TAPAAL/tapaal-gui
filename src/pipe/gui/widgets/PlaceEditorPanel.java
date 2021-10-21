package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.*;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;

import dk.aau.cs.gui.TabContent;
import dk.aau.cs.gui.undo.*;
import net.tapaal.swinghelpers.CustomJSpinner;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.SwingHelper;
import net.tapaal.swinghelpers.WidthAdjustingComboBox;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.gui.Context;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimeInvariant;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.RequireException;

import static net.tapaal.swinghelpers.GridBagHelper.Anchor.*;
import static net.tapaal.swinghelpers.GridBagHelper.Fill.HORIZONTAL;

public class PlaceEditorPanel extends javax.swing.JPanel {

	private final JRootPane rootPane;
	
	private JCheckBox sharedCheckBox;
	private JCheckBox makeNewSharedCheckBox;
	private WidthAdjustingComboBox<TimedPlace> sharedPlacesComboBox;

	private final TimedPlaceComponent place;
	private final Context context;
	private boolean makeNewShared = false;
	private boolean doNewEdit = true;
	private  boolean doOKChecked = false;
	private final TabContent currentTab;
	
	private Vector<TimedPlace> sharedPlaces;
	private final int maxNumberOfPlacesToShowAtOnce = 20;

	public PlaceEditorPanel(JRootPane rootPane, TimedPlaceComponent placeComponent, Context context) {
		this.rootPane = rootPane;
		currentTab = context.tabContent();
		place = placeComponent;
		this.context = context;
		initComponents();
		hideTimedInformation();
	}

	private void hideTimedInformation(){
        if(!place.isTimed()) {
            timeInvariantPanel.setVisible(false);
        }
    }

	private void initComponents() {
		setLayout(new java.awt.GridBagLayout());

		initBasicPropertiesPanel();
		GridBagConstraints gridBagConstraints = GridBagHelper.as(0,0, WEST, HORIZONTAL, new Insets(5, 8, 0, 8));
		add(basicPropertiesPanel, gridBagConstraints);

		initTimeInvariantPanel();

		gridBagConstraints = GridBagHelper.as(0,1, WEST, HORIZONTAL, new Insets(0, 8, 0, 8));
		add(timeInvariantPanel, gridBagConstraints);


		initButtonPanel();

		gridBagConstraints = GridBagHelper.as(0,2, new Insets(0, 8, 5, 8));
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(buttonPanel, gridBagConstraints);
	}

	private void initButtonPanel() {
		java.awt.GridBagConstraints gridBagConstraints;
		buttonPanel = new javax.swing.JPanel();
		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton = new javax.swing.JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));

		okButton.addActionListener(evt -> {
			if(doOK()){
				exit();
			}
		});
		rootPane.setDefaultButton(okButton);

		cancelButton = new javax.swing.JButton();
		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.addActionListener(evt -> {
            if (doOKChecked) {
                context.undoManager().undo();
            }
		    exit();
        });

		gridBagConstraints = GridBagHelper.as(0,0,EAST, new Insets(5, 5, 5, 5));
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;

		buttonPanel.add(cancelButton, gridBagConstraints);



		gridBagConstraints = GridBagHelper.as(1,0, WEST, new Insets(5, 5, 5, 5));
		buttonPanel.add(okButton, gridBagConstraints);

		setupInitialState();
		if(place.underlyingPlace().isShared()){
			switchToNameDropDown();
		}else{
			switchToNameTextField();
		}
	}

	private void setupInitialState() {
		sharedPlaces = new Vector<TimedPlace>(context.network().sharedPlaces());

		Collection<TimedPlace> usedPlaces = context.activeModel().places();

		sharedPlaces.removeAll(usedPlaces);
		if (place.underlyingPlace().isShared()){
			sharedPlaces.add(place.underlyingPlace());
		}

		sharedPlaces.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));
		sharedPlacesComboBox.setModel(new DefaultComboBoxModel<>(sharedPlaces));
		if(place.underlyingPlace().isShared()) {

			sharedPlacesComboBox.setSelectedItem(place.underlyingPlace());
		}

		sharedCheckBox.setEnabled(sharedPlaces.size() > 0 && !hasArcsToSharedTransitions(place.underlyingPlace()));
		sharedCheckBox.setSelected(place.underlyingPlace().isShared());
		
		makeSharedButton.setEnabled(!sharedCheckBox.isSelected() && !hasArcsToSharedTransitions(place.underlyingPlace()));

		nameTextField.setText(place.underlyingPlace().name());
		nameTextField.selectAll();
		attributesCheckBox.setSelected(place.getAttributesVisible());

		setMarking(place.underlyingPlace().numberOfTokens());
		setInvariantControlsBasedOn(place.underlyingPlace().invariant());		
	}

	private boolean hasArcsToSharedTransitions(TimedPlace underlyingPlace) {
		for(TimedInputArc arc : context.activeModel().inputArcs()){
			if(arc.source().equals(underlyingPlace) && arc.destination().isShared()) return true;
		}

		for(TimedOutputArc arc : context.activeModel().outputArcs()){
			if(arc.destination().equals(underlyingPlace) && arc.source().isShared()) return true;
		}

		for(TransportArc arc : context.activeModel().transportArcs()){
			if(arc.source().equals(underlyingPlace) && arc.transition().isShared()) return true;
			if(arc.destination().equals(underlyingPlace) && arc.transition().isShared()) return true;
		}

		for(TimedInhibitorArc arc : context.activeModel().inhibitorArcs()){
			if(arc.source().equals(underlyingPlace) && arc.destination().isShared()) return true;
		}

		return false;
	}

	private void initBasicPropertiesPanel() {
		basicPropertiesPanel = new JPanel();
		basicPropertiesPanel.setLayout(new java.awt.GridBagLayout());
		basicPropertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Place"));

		sharedCheckBox = new JCheckBox("Shared");
		sharedCheckBox.addActionListener(arg0 -> {
			JCheckBox box = (JCheckBox)arg0.getSource();
			if(box.isSelected()){
				switchToNameDropDown();
				makeSharedButton.setEnabled(false);
			}else{
				switchToNameTextField();
				nameTextField.setText(place.underlyingPlace().isShared()? CreateGui.getDrawingSurface().getNameGenerator().getNewPlaceName(context.activeModel()) : place.getName());
				makeSharedButton.setEnabled(true);
			}
		});

		GridBagConstraints gridBagConstraints = GridBagHelper.as(2,1, WEST, new Insets(0, 3, 3, 3));
		basicPropertiesPanel.add(sharedCheckBox, gridBagConstraints);

		makeSharedButton = new javax.swing.JButton();
		makeSharedButton.setText("Make shared");
		makeSharedButton.setMaximumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setMinimumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setPreferredSize(new java.awt.Dimension(110, 25));
		
		makeSharedButton.addActionListener(evt -> {
			makeNewShared = true;
            makeSharedButton.setEnabled(false);
			if(doOK()){
				setupInitialState();
				sharedCheckBox.setEnabled(true);
				sharedCheckBox.setSelected(true);
				switchToNameDropDown();
				sharedPlacesComboBox.setSelectedItem(place.underlyingPlace());
			} else {
                makeSharedButton.setEnabled(true);
                doOKChecked = false;
            }
		});
		
		gridBagConstraints = GridBagHelper.as(3,1, WEST, new Insets(5, 5, 5, 5));
		basicPropertiesPanel.add(makeSharedButton, gridBagConstraints);
		
		nameLabel = new javax.swing.JLabel("Name:");
		nameLabel = new javax.swing.JLabel("Name:");
		gridBagConstraints = GridBagHelper.as(0,1, EAST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(nameLabel, gridBagConstraints);

		nameTextField = new javax.swing.JTextField();
        SwingHelper.setPreferredWidth(nameTextField,290);

		sharedPlacesComboBox = new WidthAdjustingComboBox(maxNumberOfPlacesToShowAtOnce);

		SwingHelper.setPreferredWidth(sharedPlacesComboBox,290);

		sharedPlacesComboBox.addItemListener(e -> {
			SharedPlace place = (SharedPlace)e.getItem();
			if(place.getComponentsUsingThisPlace().size() > 0){
				setMarking(place.numberOfTokens());
			}
			setInvariantControlsBasedOn(place);
		});

		markingLabel = new javax.swing.JLabel("Marking:");
		gridBagConstraints = GridBagHelper.as(0,2, EAST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(markingLabel, gridBagConstraints);

		markingSpinner = new CustomJSpinner(0, okButton);
		gridBagConstraints = GridBagHelper.as(1,2, WEST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(markingSpinner, gridBagConstraints);

		attributesCheckBox = new javax.swing.JCheckBox("Show place name");
		attributesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		attributesCheckBox.setMargin(new Insets(0, 0, 0, 0));

		gridBagConstraints = GridBagHelper.as(1,3,WEST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(attributesCheckBox, gridBagConstraints);
	}

	private boolean isUrgencyOK(){
		for(TransportArc arc : CreateGui.getCurrentTab().currentTemplate().model().transportArcs()){
			if(arc.destination().equals(place.underlyingPlace()) && arc.transition().isUrgent()){
				JOptionPane.showMessageDialog(rootPane, "Transport arcs going through urgent transitions cannot have an invariant at the destination.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if(place.underlyingPlace().isShared()){
			for(Template t : CreateGui.getCurrentTab().allTemplates()){
				for(TransportArc arc : t.model().transportArcs()){
					if(arc.destination().equals(place.underlyingPlace()) && arc.transition().isUrgent()){
						JOptionPane.showMessageDialog(rootPane, "Transport arcs going through urgent transitions cannot have an invariant at the destination.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
		}
		return true;
	}

	private void initTimeInvariantPanel() {
		timeInvariantPanel = new JPanel();
		timeInvariantPanel.setLayout(new java.awt.GridBagLayout());
		timeInvariantPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Age Invariant"));

		invariantGroup = new JPanel(new GridBagLayout());
		invRelationNormal = new JComboBox<>(new String[] { "<=", "<" });
		invRelationConstant = new JComboBox<>(new String[] { "<=", "<" });
		//invariantSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		invariantSpinner = new CustomJSpinner(0, okButton);
		invariantSpinner.addChangeListener(e -> {
			if(!invariantInf.isSelected()){
				if ((Integer) invariantSpinner.getValue() < 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
					invRelationNormal.setSelectedItem("<=");
				} else if (invRelationNormal.getModel().getSize() == 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
				}
			}
		});

		GridBagConstraints gbc = GridBagHelper.as(1,0, HORIZONTAL, new Insets(3, 3, 3, 3));
		invariantGroup.add(invRelationNormal, gbc);

		gbc = GridBagHelper.as(1,1, HORIZONTAL, new Insets(3, 3, 3, 3));
        invariantGroup.add(invRelationConstant, gbc);

		gbc = GridBagHelper.as(2,0, new Insets(3, 3, 3, 3));
		invariantGroup.add(invariantSpinner, gbc);

		invariantInf = new JCheckBox(Character.toString('\u221e'));
		invariantInf.addActionListener(arg0 -> {
			if(!isUrgencyOK()){
				invariantInf.setSelected(true);
				return;
			}
			if (!invariantInf.isSelected()) {
				invRelationNormal.setEnabled(true);
				invariantSpinner.setEnabled(true);
				invRelationNormal.setSelectedItem("<=");
				if ((Integer) invariantSpinner.getValue() < 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
				} else {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
				}
			} else {
				invRelationNormal.setEnabled(false);
				invariantSpinner.setEnabled(false);
				invRelationNormal.setSelectedItem("<");
				invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
			}

		});
		gbc = GridBagHelper.as(3,0);
		invariantGroup.add(invariantInf, gbc);

		Set<String> constants = context.network().getConstantNames();
		String[] constantArray = constants.toArray(new String[constants.size()]);
		Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);

		invConstantsComboBox = new WidthAdjustingComboBox<>(maxNumberOfPlacesToShowAtOnce);
		invConstantsComboBox.setModel(new DefaultComboBoxModel<>(constantArray));
		//	invConstantsComboBox = new JComboBox(constants.toArray());
		invConstantsComboBox.setMaximumRowCount(20);
		//	invConstantsComboBox.setMinimumSize(new Dimension(100, 30));
		invConstantsComboBox.setPreferredSize(new Dimension(230, 30));
		invConstantsComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setRelationModelForConstants();
			}
		});

		gbc = GridBagHelper.as(2,1, WEST);
		invariantGroup.add(invConstantsComboBox, gbc);

		normalInvRadioButton = new JRadioButton("Normal");
		normalInvRadioButton.addActionListener(e -> {
			disableInvariantComponents();
			enableNormalInvariantComponents();
		});

		constantInvRadioButton = new JRadioButton("Constant");
		constantInvRadioButton.addActionListener(e -> {
			disableInvariantComponents();
			enableConstantInvariantComponents();
		});
		if (constants.isEmpty()){
			constantInvRadioButton.setEnabled(false);
		}
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(normalInvRadioButton);
		btnGroup.add(constantInvRadioButton);

		gbc = GridBagHelper.as(0,0, WEST);
		invariantGroup.add(normalInvRadioButton, gbc);

		gbc = GridBagHelper.as(0,1, WEST);
		invariantGroup.add(constantInvRadioButton, gbc);

		TimeInvariant invariantToSet = place.getInvariant();

		if (invariantToSet.isUpperNonstrict()) {
			invRelationNormal.setSelectedItem("<=");
		} else {
			invRelationNormal.setSelectedItem("<");
		}

		if (invariantToSet.upperBound() instanceof InfBound) {
			invariantSpinner.setEnabled(false);
			invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
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
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
				} else {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
				}
				invariantSpinner.setValue(invariantToSet.upperBound().value());
				invariantSpinner.setEnabled(true);
				invRelationNormal.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
				invariantInf.setSelected(false);
			}
		}

		GridBagConstraints gridBagConstraints = GridBagHelper.as(1,4,2, WEST, new Insets(3, 3, 3, 3));
		timeInvariantPanel.add(invariantGroup, gridBagConstraints);
	}

	private void setRelationModelForConstants() {
		int value = CreateGui.getCurrentTab().network().getConstantValue(invConstantsComboBox.getSelectedItem().toString());

		String selected = invRelationConstant.getSelectedItem().toString();
		if (value == 0) {
			invRelationConstant.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
		} else {
			invRelationConstant.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
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
		invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
	}

	protected void disableInvariantComponents() {
		invRelationNormal.setEnabled(false);
		invRelationConstant.setEnabled(false);
		invariantSpinner.setEnabled(false);
		invConstantsComboBox.setEnabled(false);
		invariantInf.setEnabled(false);
	}

	private void switchToNameTextField() {
		basicPropertiesPanel.remove(sharedPlacesComboBox);
		GridBagConstraints gbc = GridBagHelper.as(1,1, HORIZONTAL, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(nameTextField, gbc);

		basicPropertiesPanel.validate();
		basicPropertiesPanel.repaint();
	}

	private void switchToNameDropDown() {
		basicPropertiesPanel.remove(nameTextField);
		GridBagConstraints gbc = GridBagHelper.as(1,1, HORIZONTAL, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(sharedPlacesComboBox, gbc);

		basicPropertiesPanel.validate();
		basicPropertiesPanel.repaint();

		SharedPlace selected = (SharedPlace)sharedPlacesComboBox.getSelectedItem();
		setInvariantControlsBasedOn(selected);
		if(selected.getComponentsUsingThisPlace().size() > 0){
			setMarking(selected.numberOfTokens());
		}
	}

	private void setMarking(int numberOfTokens) {
		markingSpinner.setValue(numberOfTokens);
	}

	private void setInvariantControlsBasedOn(TimedPlace place) {
		if(place instanceof SharedPlace && ((SharedPlace) place).getComponentsUsingThisPlace().size() > 0){
			setInvariantControlsBasedOn(place.invariant());
		}
	}
	
	private void setInvariantControlsBasedOn(TimeInvariant invariant) {
		if(invariant.upperBound() instanceof ConstantBound){
			constantInvRadioButton.setSelected(true);
			invRelationConstant.setModel(new DefaultComboBoxModel<>(invariant.upperBound().value() == 0 ? new String[] { "<=" } : new String[] { "<", "<=" }));
			invRelationConstant.setSelectedItem(invariant.isUpperNonstrict() ? "<=" : "<");
			invRelationConstant.setEnabled(true);
			invConstantsComboBox.setEnabled(true);
			invConstantsComboBox.setSelectedItem(((ConstantBound)invariant.upperBound()).constant());
		}else{
			normalInvRadioButton.setSelected(true);
			if(invariant.upperBound() instanceof InfBound){
				invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
				invariantSpinner.setValue(0);
				invRelationNormal.setEnabled(false);
				invariantSpinner.setEnabled(false);
				invariantInf.setSelected(true);
			}else{
				if(invariant.upperBound().value() == 0 && !invariantInf.isSelected()){
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
				}else{
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<", "<=" }));
				}
				invRelationNormal.setSelectedItem(invariant.isUpperNonstrict() ? "<=" : "<");
				invariantSpinner.setValue(invariant.upperBound().value());
				invRelationNormal.setEnabled(true);
				invariantSpinner.setEnabled(true);
				invariantInf.setSelected(false);
			}
		}
	}

	private boolean doOK() {
		// Check urgent constrain
		if(!invariantInf.isSelected() && !isUrgencyOK()){
			return false;
		}

		int newMarking = (Integer)markingSpinner.getValue();
		if (newMarking > Pipe.MAX_NUMBER_OF_TOKENS_ALLOWED) {
			JOptionPane.showMessageDialog(this,"It is allowed to have at most " + Pipe.MAX_NUMBER_OF_TOKENS_ALLOWED + " tokens in a place.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//Only make new edit if it has not already been done
		if(doNewEdit) {
			context.undoManager().newEdit(); // new "transaction""
			doNewEdit = false;
		}
		TimedPlace underlyingPlace = place.underlyingPlace();

		SharedPlace selectedPlace = (SharedPlace)sharedPlacesComboBox.getSelectedItem();
		if(sharedCheckBox.isSelected() && !selectedPlace.equals(underlyingPlace)){
			Command command = new MakePlaceSharedCommand(context.activeModel(), selectedPlace, place.underlyingPlace(), place, context.tabContent());
			context.undoManager().addEdit(command);
			try{
				command.redo();
			}catch(RequireException e){
				context.undoManager().undo();
				JOptionPane.showMessageDialog(this,"Another place in the same component is already shared under that name", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}		
		}else if(!sharedCheckBox.isSelected()){
			if(underlyingPlace.isShared()){
				String uniqueName = context.nameGenerator().getNewPlaceName(context.activeModel());
				Command unshareCmd = new UnsharePlaceCommand(context.activeModel(), (SharedPlace)underlyingPlace, new LocalTimedPlace(uniqueName), place);
				unshareCmd.redo();
				context.undoManager().addEdit(unshareCmd);
			}

			String newName = nameTextField.getText();
			String oldName = place.underlyingPlace().name();
			if(context.activeModel().isNameUsed(newName) && !oldName.equalsIgnoreCase(newName)){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this, "The specified name is already used by another place or transition.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}   	

			if (!oldName.equals(newName)) {
                Command renameCommand = new RenameTimedPlaceCommand(context.tabContent(), (LocalTimedPlace) place.underlyingPlace(), oldName, newName);
                context.undoManager().addEdit(renameCommand);
                try { // set name
                    renameCommand.redo();
                } catch (RequireException e) {
                    context.undoManager().undo();
                    JOptionPane.showMessageDialog(this, "Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                context.nameGenerator().updateIndices(context.activeModel(), newName);
            }
		
			if(makeNewShared && !makeSharedButton.isEnabled()){
				Command command = new MakePlaceNewSharedCommand(context.activeModel(), newName, place.underlyingPlace(), place, context.tabContent(), false);
				context.undoManager().addEdit(command);
				try{
					command.redo();
				}catch(RequireException e){
					context.undoManager().undo();
					//This is checked as a place cannot be shared if there exists a transition with the same name
					if(context.activeModel().parentNetwork().isNameUsedForPlacesOnly(newName)) {
						int dialogResult = JOptionPane.showConfirmDialog(this, "A place with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords. \n\nThis place name will be changed into shared one also in all other components.", "Error", JOptionPane.OK_CANCEL_OPTION);
						if(dialogResult == JOptionPane.OK_OPTION) {
							Command cmd = new MakePlaceNewSharedMultiCommand(context, newName, place);	
							cmd.redo();
							context.undoManager().addNewEdit(cmd);
						} else {
							return false;
						}
					} else {
						JOptionPane.showMessageDialog(this, "A transition with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}	
			}
		}

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

		if ((place.getAttributesVisible() && !attributesCheckBox.isSelected()) || (!place.getAttributesVisible() && attributesCheckBox.isSelected())) {
			place.toggleAttributesVisible();

            Map<PetriNetObject, Boolean> map = new HashMap<>();
            map.put(place, !place.getAttributesVisible());

            Command changeVisibility = new ChangeAllNamesVisibilityCommand(currentTab, map, null, place.getAttributesVisible());
			context.undoManager().addEdit(changeVisibility);
		}
		place.update(true);
		place.repaint();

		context.network().buildConstraints();

        doOKChecked = true;

        return true;
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
	private javax.swing.JButton makeSharedButton;
	private javax.swing.JPanel basicPropertiesPanel;
	private javax.swing.JPanel timeInvariantPanel;
	private JPanel invariantGroup;
	private JComboBox<String> invRelationNormal;
	private JComboBox<String> invRelationConstant;
	private JSpinner invariantSpinner;
	private JCheckBox invariantInf;
	private JComboBox<String> invConstantsComboBox;
	private JRadioButton normalInvRadioButton;
	private JRadioButton constantInvRadioButton;
}

