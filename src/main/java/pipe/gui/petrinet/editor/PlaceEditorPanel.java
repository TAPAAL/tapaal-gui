package pipe.gui.petrinet.editor;

import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import net.tapaal.gui.petrinet.Context;
import java.awt.event.ItemEvent;
import java.util.*;

import net.tapaal.gui.petrinet.undo.*;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.editor.ColorComboBoxRenderer;
import net.tapaal.gui.petrinet.undo.Colored.ColoredPlaceMarkingEditCommand;
import net.tapaal.gui.petrinet.undo.Colored.SetArcExpressionCommand;
import net.tapaal.gui.petrinet.undo.Colored.SetColoredArcIntervalsCommand;
import net.tapaal.gui.petrinet.undo.Colored.SetTransportArcExpressionsCommand;
import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.RequireException;
import net.tapaal.swinghelpers.CustomJSpinner;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.SwingHelper;
import net.tapaal.swinghelpers.WidthAdjustingComboBox;
import net.tapaal.gui.petrinet.Template;
import net.tapaal.gui.petrinet.editor.ColorComboboxPanel;
import net.tapaal.gui.petrinet.editor.ColoredTimeInvariantDialogPanel;
import pipe.gui.Constants;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.List;

import static net.tapaal.swinghelpers.GridBagHelper.Anchor.EAST;
import static net.tapaal.swinghelpers.GridBagHelper.Anchor.WEST;

import pipe.gui.swingcomponents.EscapableDialog;

import static net.tapaal.swinghelpers.GridBagHelper.Fill.HORIZONTAL;

public class PlaceEditorPanel extends JPanel {

	private final JRootPane rootPane;
	
	private JCheckBox sharedCheckBox;
    private WidthAdjustingComboBox sharedPlacesComboBox;

	private final TimedPlaceComponent place;
	private final Context context;
	private boolean makeNewShared = false;
	private boolean doNewEdit = true;
	private boolean doOKChecked = false;
	private boolean editSharedPlace = false;
	private final PetriNetTab currentTab;
	private final EscapableDialog parent;
	private final JPanel mainPanel;

    private Vector<TimedPlace> sharedPlaces;
	private final int maxNumberOfPlacesToShowAtOnce = 20;
	protected final ArcExpression originalExpression;

	public PlaceEditorPanel(EscapableDialog parent,JRootPane rootPane, TimedPlaceComponent placeComponent, Context context) {
		this.rootPane = rootPane;
		currentTab = context.tabContent();
		place = placeComponent;
		this.context = context;
        this.parent = parent;
		this.colorType = place.underlyingPlace().getColorType();
		setLayout(new BorderLayout());
		mainPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane();
		initComponents();
		if (place.underlyingPlace().getTokensAsExpression() != null) originalExpression = place.underlyingPlace().getTokensAsExpression().deepCopy();
        else originalExpression = null;
		hideIrrelevantInformation();
		scrollPane.setViewportView(mainPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.CENTER);
	}

	private void hideIrrelevantInformation(){
        if(!place.isTimed()) {
            timeInvariantPanel.setVisible(false);
            timeInvariantColorPanel.setVisible(false);
        }
        if(!place.isColored()){
            timeInvariantColorPanel.setVisible(false);
            tokenPanel.setVisible(false);
            colorTypePanel.setVisible(false);
        }
        if(place.isColored()){
            markingLabel.setVisible(false);
            markingSpinner.setVisible(false);
            if(place.isTimed()){
                timeInvariantPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Default Age Invariant"));
            }
        }
    }

	private void initComponents() {
		initBasicPropertiesPanel();
		GridBagConstraints gridBagConstraints = GridBagHelper.as(0,0, WEST, HORIZONTAL, new Insets(5, 8, 0, 8));
		gridBagConstraints.weightx = 1.0;
		mainPanel.add(basicPropertiesPanel, gridBagConstraints);

		initTimeInvariantPanel();

		gridBagConstraints = GridBagHelper.as(0,2, WEST, HORIZONTAL, new Insets(0, 8, 0, 8));
		mainPanel.add(timeInvariantPanel, gridBagConstraints);
        initColorTypePanel();
        initColorInvariantPanel();
        initTokensPanel();
        setInitialComboBoxValue();
        
        writeTokensToList(place.underlyingPlace());
        setColoredTimeInvariants(place.underlyingPlace());

		initButtonPanel();

		gridBagConstraints = GridBagHelper.as(0,5, new Insets(0, 8, 5, 8));
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		mainPanel.add(buttonPanel, gridBagConstraints);


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
            place.underlyingPlace().setTokenExpression(originalExpression);
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
		sharedPlaces = new Vector<>(context.network().sharedPlaces());

		Collection<TimedPlace> usedPlaces = context.activeModel().places();

		sharedPlaces.removeAll(usedPlaces);
		if (place.underlyingPlace().isShared()) {
			sharedPlaces.add(place.underlyingPlace());
		}

		sharedPlaces.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));
		sharedPlacesComboBox.setModel(new DefaultComboBoxModel<>(sharedPlaces));
		if (place.underlyingPlace().isShared()) {
			sharedPlacesComboBox.setSelectedItem(place.underlyingPlace());
		}

		sharedCheckBox.setEnabled(sharedPlaces.size() > 0);
		sharedCheckBox.setSelected(place.underlyingPlace().isShared());
		
		makeSharedButton.setEnabled(!sharedCheckBox.isSelected());

		nameTextField.setText(place.underlyingPlace().name());
		nameTextField.selectAll();
		attributesCheckBox.setSelected(place.getAttributesVisible());

		setMarking(place.underlyingPlace().numberOfTokens());
		setInvariantControlsBasedOn(place.underlyingPlace().invariant());		
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
				nameTextField.setText(place.underlyingPlace().isShared()? context.nameGenerator().getNewPlaceName(context.activeModel()) : place.getName());
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
		    editSharedPlace = true;
			SharedPlace place = (SharedPlace)e.getItem();
			if (place.getComponentsUsingThisPlace().size() > 0) {
			    if (currentTab.lens.isColored()) {
                    colorTypeComboBox.setSelectedItem(place.getColorType());
                    coloredTokenListModel.clear();

                    ArcExpression expr = place.getTokensAsExpression();
                    if (expr != null) {
                        for (ExprStringPosition child : expr.getChildren()) {
                            if (child.getObject() instanceof NumberOfExpression) {
                                coloredTokenListModel.addElement((NumberOfExpression) child.getObject());
                            }
                        }
                    }
                }
				setMarking(place.numberOfTokens());
			}
			setInvariantControlsBasedOn(place);
            editSharedPlace = false;
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
		for(TransportArc arc : context.activeModel().transportArcs()){
			if(arc.destination().equals(place.underlyingPlace()) && arc.transition().isUrgent()){
				JOptionPane.showMessageDialog(rootPane, "Transport arcs going through urgent transitions cannot have an invariant at the destination.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if(place.underlyingPlace().isShared()){
			for(Template t : context.tabContent().allTemplates()){
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
		String[] constantArray = constants.toArray(new String[0]);
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
		int value = context.network().getConstantValue(Objects.requireNonNull(invConstantsComboBox.getSelectedItem()).toString());

		String selected = Objects.requireNonNull(invRelationConstant.getSelectedItem()).toString();
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
		setColorControlsBasedOn(selected);
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
    private void setColorControlsBasedOn(TimedPlace place) {
        if(place instanceof SharedPlace && ((SharedPlace) place).getComponentsUsingThisPlace().size() > 0){
            colorTypeComboBox.setSelectedItem(place.getColorType());
            writeTokensToList(place);
            setColoredTimeInvariants(place);
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
        
        if (place.isColored()) {
            newMarking = (int)addTokenSpinner.getValue();
        }

		if (newMarking > Constants.MAX_NUMBER_OF_TOKENS_ALLOWED) {
			JOptionPane.showMessageDialog(this,"It is allowed to have at most " + Constants.MAX_NUMBER_OF_TOKENS_ALLOWED + " tokens in a place.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//Only make new edit if it has not already been done
		if(doNewEdit) {
			context.undoManager().newEdit(); // new "transaction""
			doNewEdit = false;
		}
		TimedPlace underlyingPlace = place.underlyingPlace();

		SharedPlace selectedPlace = (SharedPlace)sharedPlacesComboBox.getSelectedItem();

        Command sharedCommand = null;
		if(sharedCheckBox.isSelected() && !Objects.equals(selectedPlace, underlyingPlace)){
			sharedCommand = new MakePlaceSharedCommand(context.activeModel(), selectedPlace, place.underlyingPlace(), place, context.tabContent());
			context.undoManager().addEdit(sharedCommand);
			try{
				sharedCommand.redo();
			}catch(RequireException e){
				context.undoManager().undo();
                doNewEdit = true;
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
			if(context.activeModel().isNameUsed(newName) && !oldName.equals(newName)){
				context.undoManager().undo(); 
                doNewEdit = true;
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
                    doNewEdit = true;
                    JOptionPane.showMessageDialog(this, "Acceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                context.nameGenerator().updateIndices(context.activeModel(), newName);
            }

			if(makeNewShared){
				Command command = new MakePlaceNewSharedCommand(context.activeModel(), newName, place.underlyingPlace(),
                    place, context.tabContent(), false);
				context.undoManager().addEdit(command);
				try{
					command.redo();
				}catch(RequireException e){
					context.undoManager().undo();
                    doNewEdit = true;
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
        doOkColors(newMarking);

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

        SharedPlace placeBefore = sharedCheckBox.isSelected() ? (SharedPlace)place.underlyingPlace() : null;
        boolean sameSharedAsBefore = placeBefore != null && selectedPlace.equals(placeBefore);
        if (sharedCheckBox.isSelected() && sameSharedAsBefore) {
            boolean success = SharedElementSynchronizer.updateSharedArcs(place);
            if (!success) {
                sharedCommand.undo();
                context.undoManager().removeCurrentEdit();
                doNewEdit = true;
                JOptionPane.showMessageDialog(
                    this,
                    "An arc between two shared nodes conflicts with an existing arc in another component.\nDelete the arc in all but one of the components to resolve the conflict.",
                    "Error", JOptionPane.ERROR_MESSAGE);
    
                return false;
            }
        }

        doOKChecked = true;
        
        if (context.undoManager().currentEditIsEmpty()) {
            context.undoManager().removeCurrentEdit();
        }

        return true;
	}

	private void doOkColors(int newMarking){
        if (!place.isColored()) {
            if(newMarking != place.underlyingPlace().numberOfTokens()){
                Command command = new TimedPlaceMarkingEditCommand(place, newMarking - place.underlyingPlace().numberOfTokens());
                command.redo();
                context.undoManager().addEdit(command);
                return;
            }
        } else {
            int oldTokenCount = place.underlyingPlace().numberOfTokens();
            ArrayList<TimedToken> tokensToAdd = new ArrayList<>();
            ArrayList<TimedToken> oldTokenList = new ArrayList<>(context.activeModel().marking().getTokensFor(place.underlyingPlace()));
            List<ColoredTimeInvariant> ctiList = new ArrayList<>();
            Vector<ArcExpression> v = new Vector<>();

            for (int i = 0; i < coloredTokenListModel.getSize(); i++) {
                v.add(coloredTokenListModel.getElementAt(i));
            }

            AddExpression newExpression = null;
            if (!v.isEmpty()) {
                newExpression = new AddExpression(v);
                ColorMultiset cm = newExpression.eval(context.network().getContext());
                if (cm != null) {
                    tokensToAdd.addAll(cm.getTokens(place.underlyingPlace()));
                }
            } else {
                place.underlyingPlace().resetNumberOfTokensColor();
            }
            
            for (int i = 0; i < timeConstraintListModel.size(); i++) {
                ctiList.add(timeConstraintListModel.get(i));
            }
            if (!colorType.equals(place.underlyingPlace().getColorType())) {
                updateArcsAccordingToColorType();
            }

            TimedPlace underlyingPlace = place.underlyingPlace();

            boolean anyChanges = !underlyingPlace.getCtiList().equals(ctiList) ||
                                 !underlyingPlace.getColorType().equals(colorType) ||
                                 !oldTokenList.equals(tokensToAdd) ||
                                 originalExpression != null &&
                                 !originalExpression.equals(newExpression) ||
                                 !(oldTokenCount == underlyingPlace.numberOfTokens());

            if (anyChanges) { 
                Command command = new ColoredPlaceMarkingEditCommand(oldTokenList, tokensToAdd, originalExpression, newExpression, context, place, ctiList, colorType, oldTokenCount, place.underlyingPlace().numberOfTokens());
                command.redo();
                context.undoManager().addEdit(command);
            }
        }
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
    private void initTokensPanel() {
        tokenPanel = new JPanel();
        tokenButtonPanel = new JPanel(new GridBagLayout());
        tokenPanel.setLayout(new GridBagLayout());
        tokenPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tokens"));

        tokenColorComboboxPanel = new ColorComboboxPanel(colorType,true) {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                updateSpinnerValue(true);
            }
        };
        tokenColorComboboxPanel.removeScrollPaneBorder();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        tokenPanel.add(tokenColorComboboxPanel, gbc);
        //Logger.log(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0].getItemAt(0).toString());


        coloredTokenListModel = new DefaultListModel();
        tokenList = new JList(coloredTokenListModel);
        tokenList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tokenList.addListSelectionListener(listSelectionEvent -> {
            if(!listSelectionEvent.getValueIsAdjusting() && !tokenList.isSelectionEmpty()) {
                tokenColorComboboxPanel.updateSelection(((NumberOfExpression)tokenList.getSelectedValue()).getColor().get(0));
                updateSpinnerValue(false);
                addColoredTokenButton.setText("Modify");
                removeColoredTokenButton.setEnabled(true);
            } else if(tokenList.isSelectionEmpty()){
                addColoredTokenButton.setText("Add");
                removeColoredTokenButton.setEnabled(false);
            }
        });

        JScrollPane tokenListScrollPane = new JScrollPane(tokenList);
        tokenListScrollPane.setViewportView(tokenList);
        tokenListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension tokenScrollPaneDim = new Dimension(100, 150);
        tokenListScrollPane.setBorder(BorderFactory.createTitledBorder( "Tokens in initial marking"));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(3, 3, 3,3);
        tokenListScrollPane.setPreferredSize(tokenScrollPaneDim);
        //tokenListScrollPane.setMinimumSize(new Dimension(700,100));
        tokenPanel.add(tokenListScrollPane, gbc);

        addColoredTokenButton = new JButton("Add");
        Dimension buttonSize = new Dimension(100, 30);
        addColoredTokenButton.setPreferredSize(buttonSize);
        addColoredTokenButton.setMinimumSize(buttonSize);
        addColoredTokenButton.setMaximumSize(buttonSize);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(3, 3, 3,3);
        tokenButtonPanel.add(addColoredTokenButton, gbc);

        addColoredTokenButton.addActionListener(actionEvent -> {    
            int tokenSpinnerValue = (int)addTokenSpinner.getValue();
            
            int tokenListSum = 0;

            for (int i = 0; i < coloredTokenListModel.size(); ++i) {
                if (i != tokenList.getSelectedIndex()) {
                    tokenListSum += Integer.parseInt(coloredTokenListModel.getElementAt(i).toString().split("'")[0]);
                }
            }

            tokenListSum += tokenSpinnerValue;

            if (tokenListSum > Constants.MAX_NUMBER_OF_TOKENS_ALLOWED) {
                JOptionPane.showMessageDialog(this,"It is allowed to have at most " + Constants.MAX_NUMBER_OF_TOKENS_ALLOWED + " tokens in a place.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            NumberOfExpression exprToAdd = buildTokenExpression(tokenSpinnerValue);

            addTokenExpression(exprToAdd);
            addColoredTokenButton.setText("Modify");
            if(tokenList.isSelectionEmpty()){
                tokenList.setSelectedIndex(coloredTokenListModel.size()-1);
            }
        });
        addTokenSpinner = new CustomJSpinner(1, 1, Integer.MAX_VALUE);

        addTokenSpinner.setPreferredSize(buttonSize);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3,3);
        tokenPanel.add(addTokenSpinner, gbc);

        removeColoredTokenButton = new JButton("Remove");

        removeColoredTokenButton.setPreferredSize(buttonSize);
        removeColoredTokenButton.setMinimumSize(buttonSize);
        removeColoredTokenButton.setMaximumSize(buttonSize);

        removeColoredTokenButton.addActionListener(actionEvent -> {
            if(tokenList.getSelectedIndex() > -1){
                int index = tokenList.getSelectedIndex();
                coloredTokenListModel.remove(tokenList.getSelectedIndex());
                updateTokenSelection(index);
            }

        });
        removeColoredTokenButton.setEnabled(tokenList.getSelectedIndex() > 0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        tokenButtonPanel.add(removeColoredTokenButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tokenPanel.add(tokenButtonPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        mainPanel.add(tokenPanel, gbc);
    }

    private void updateTokenSelection(int index) {
	    int currentSize = tokenList.getModel().getSize();
	    if (currentSize > index)
	        tokenList.setSelectedIndex(index);
	    else if (currentSize != 0)
	        tokenList.setSelectedIndex(currentSize-1);
	    else {
            addColoredTokenButton.setText("Add");
            removeColoredTokenButton.setEnabled(false);
        }
    }

    public void initColorInvariantPanel(){
	    timeInvariantColorPanel = new JPanel(new GridBagLayout());

        timeInvariantColorPanel = initNonDefaultColorInvariantPanel();
        timeInvariantColorPanel.setBorder(BorderFactory.createTitledBorder("Time invariants for specific colors"));

        GridBagConstraints gbc;
        gbc = GridBagHelper.as(0,3, WEST, new Insets(3, 3, 3, 3));
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        mainPanel.add(timeInvariantColorPanel, gbc);

    }

    private JPanel initNonDefaultColorInvariantPanel() {
	    //This panel holds the edit panel and the scrollpane
        JPanel nonDefaultColorInvariantPanel = new JPanel(new GridBagLayout());
        //this panel holds the buttons, the invariant editor panel and the color combobox
        JPanel colorInvariantEditPanel = new JPanel(new GridBagLayout());

        colorInvariantComboboxPanel = new ColorComboboxPanel(colorType) {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                ColoredTimeInvariant timeConstraint;
                if (!(colorType instanceof ProductType)) {
                    timeConstraint = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR((Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex()));
                } else {
                    Vector<Color> colors = new Vector<>();
                    for (JComboBox comboBox : comboBoxes) {
                        colors.add((Color) comboBox.getItemAt(comboBox.getSelectedIndex()));
                    }
                    Color color = new Color(colorType, 0, colors);
                    timeConstraint = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR(color);
                }
                boolean alreadyExists = false;
                for (int i = 0; i < timeConstraintListModel.size(); i++) {
                    if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i))){
                        invariantEditorPanel.setInvariant(timeConstraintListModel.get(i));
                        timeConstraintList.setSelectedIndex(i);
                        addTimeConstraintButton.setText("Modify");
                        alreadyExists = true;
                    }
                }
                if(!alreadyExists){
                    invariantEditorPanel.setInvariant(timeConstraint);
                    addTimeConstraintButton.setText("Add");
                }
            }
        };
        colorInvariantComboboxPanel.removeScrollPaneBorder();
        addTimeConstraintButton = new JButton("Add");
        removeTimeConstraintButton = new JButton("Remove");

        Dimension buttonSize = new Dimension(80, 27);

        addTimeConstraintButton.setPreferredSize(buttonSize);
        removeTimeConstraintButton.setPreferredSize(buttonSize);

        timeConstraintListModel = new DefaultListModel();
        timeConstraintList = new JList(timeConstraintListModel);
        timeConstraintList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        timeConstraintListModel.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent arg0) {
            }

            public void intervalAdded(ListDataEvent arg0) {
                timeConstraintList.setSelectedIndex(arg0.getIndex0());
                timeConstraintList.ensureIndexIsVisible(arg0.getIndex0());
            }

            public void intervalRemoved(ListDataEvent arg0) {
                int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
                timeConstraintList.setSelectedIndex(index);
                timeConstraintList.ensureIndexIsVisible(index);
            }

        });
        timeConstraintList.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                JList source = (JList) listSelectionEvent.getSource();
                if(source.getSelectedIndex() >= 0){
                    ColoredTimeInvariant cti = (ColoredTimeInvariant) source.getModel().getElementAt(source.getSelectedIndex());
                    invariantEditorPanel.setInvariant(cti);
                    colorInvariantComboboxPanel.updateSelection(cti.getColor());
                    addTimeConstraintButton.setText("Modify");
                }
                removeTimeConstraintButton.setEnabled(!timeConstraintList.isSelectionEmpty());
            }
        });
        JScrollPane timeConstraintScrollPane = new JScrollPane(timeConstraintList);
        timeConstraintScrollPane.setViewportView(timeConstraintList);
        timeConstraintScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        timeConstraintScrollPane.setBorder(BorderFactory.createTitledBorder("Time invariant for colors"));

        addTimeConstraintButton.addActionListener(actionEvent -> {
            ColoredTimeInvariant timeConstraint = invariantEditorPanel.getInvariant();
            boolean alreadyExists = false;

            for (int i = 0; i < timeConstraintListModel.size(); i++) {
                if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i))){
                    alreadyExists = true;
                    timeConstraintListModel.setElementAt(timeConstraint, i);
                    timeConstraintList.setSelectedIndex(i);
                }
            }
            if (!alreadyExists){
                timeConstraintListModel.addElement(timeConstraint);
                timeConstraintList.setSelectedIndex(timeConstraintListModel.size()-1);
            }
        });

        removeTimeConstraintButton.addActionListener(actionEvent -> {
            int index = timeConstraintList.getSelectedIndex();
            timeConstraintListModel.removeElementAt(index);
            if(timeConstraintListModel.isEmpty()){
                addTimeConstraintButton.setText("Add");
            } else{
                timeConstraintList.setSelectedIndex(index);
            }

        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        colorInvariantEditPanel.add(colorInvariantComboboxPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3,3);
        buttonPanel.add(addTimeConstraintButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(removeTimeConstraintButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        ColoredTimeInvariant cti;
        if(place.underlyingPlace().getCtiList().isEmpty()) {
            cti = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR(place.underlyingPlace().getColorType().getFirstColor());
        } else{
            cti = place.underlyingPlace().getCtiList().get(0);
        }
        invariantEditorPanel = new ColoredTimeInvariantDialogPanel(rootPane,context, cti, place);
        colorInvariantEditPanel.add(invariantEditorPanel,gbc);

        gbc=new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        colorInvariantEditPanel.add(buttonPanel,gbc);

        gbc=new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        nonDefaultColorInvariantPanel.add(colorInvariantEditPanel,gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        nonDefaultColorInvariantPanel.add(timeConstraintScrollPane, gbc);

        return nonDefaultColorInvariantPanel;
    }

    private void initColorTypePanel() {
	    colorTypePanel = new JPanel();
        colorTypePanel.setLayout(new GridBagLayout());
        colorTypePanel.setBorder(new TitledBorder("Color Type"));

        JLabel colortypeLabel = new JLabel();
        colortypeLabel.setText("Color Type:");

        colorTypeComboBox = new JComboBox();
        List<ColorType> colorTypes = context.network().colorTypes();

        for (ColorType element : colorTypes) {
            colorTypeComboBox.addItem(element);
        }
        colorTypeComboBox.setRenderer(new ColorComboBoxRenderer(colorTypeComboBox));
        colorTypeComboBox.setSelectedItem(colorType);

        colorTypeComboBox.addActionListener(actionEvent -> {
            if (colorTypeComboBox.getSelectedItem() != null && colorTypeComboBox.getSelectedItem().equals(tokenColorComboboxPanel.getColorType())) {
                return;
            }
            if (!editSharedPlace && (!(coloredTokenListModel.getSize() < 1) || !timeConstraintListModel.isEmpty())){
                int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to change the color type for this place?\n" +
                    "All tokens and time invariants for colors will be deleted.","alert", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    setNewColorType(colorTypeComboBox.getItemAt(colorTypeComboBox.getSelectedIndex()));
                }
                else { // NO.OPTION - we set the color type to the previous selected one
                    for (int i = 0; i < colorTypeComboBox.getItemCount(); i++) {
                        if (colorType.getName().equals(colorTypeComboBox.getItemAt(i).getName())) {
                            colorTypeComboBox.setSelectedIndex(i);
                        }
                    }
                }
            } else {
                setNewColorType(colorTypeComboBox.getItemAt(colorTypeComboBox.getSelectedIndex()));
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        colorTypePanel.add(colortypeLabel, gbc);

        Dimension colorTypeComboBoxSize = new Dimension(500, 30);
        colorTypeComboBox.setPreferredSize(colorTypeComboBoxSize);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3,3 );
        colorTypePanel.add(colorTypeComboBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        mainPanel.add(colorTypePanel, gbc);
    }

    private void updateArcsAccordingToColorType() {
        for(Arc arc : place.getPostset()){
            //We know it goes from place to transition so it can be either InputArcComponent or TransportArc or InhibitorArc
            if(arc instanceof TimedTransportArcComponent){
                TransportArc transportArc = ((TimedTransportArcComponent)arc).underlyingTransportArc();
                Vector<ColorExpression> vecColorExpr = new Vector<>();
                vecColorExpr.add(colorType.createColorExpressionForFirstColor());
                NumberOfExpression numbExpr = new NumberOfExpression(transportArc.getOutputExpression().weight(), vecColorExpr);
                Command expressionsCommand = new SetTransportArcExpressionsCommand((TimedTransportArcComponent)arc, transportArc.getInputExpression(),
                    numbExpr, transportArc.getOutputExpression(), transportArc.getOutputExpression());
                expressionsCommand.redo();
                context.undoManager().addEdit(expressionsCommand);
            }else if(!(arc instanceof TimedInhibitorArcComponent)){
                Vector<ColorExpression> vecColorExpr = new Vector<>();
                vecColorExpr.add(colorType.createColorExpressionForFirstColor());
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                Command arcExpressionCommand = new SetArcExpressionCommand(arc,arc.getExpression(),numbExpr);
                arcExpressionCommand.redo();
                context.undoManager().addEdit(arcExpressionCommand);

            }
            if(!(arc instanceof TimedInhibitorArcComponent)){
                Command arcIntervalCommand = new SetColoredArcIntervalsCommand((TimedInputArcComponent)arc,((TimedInputArcComponent)arc).getCtiList(), new ArrayList<>());
                arcIntervalCommand.redo();
                context.undoManager().addEdit(arcIntervalCommand);
            }
        }
        for(Arc arc : place.getPreset()) {
            if(arc instanceof TimedTransportArcComponent){
                TransportArc transportArc = ((TimedTransportArcComponent)arc).underlyingTransportArc();
                Vector<ColorExpression> vecColorExpr = new Vector<>();
                vecColorExpr.add(colorType.createColorExpressionForFirstColor());
                NumberOfExpression numbExpr = new NumberOfExpression(transportArc.getInputExpression().weight(), vecColorExpr);
                Command expressionsCommand = new SetTransportArcExpressionsCommand((TimedTransportArcComponent)arc, transportArc.getInputExpression(),
                    transportArc.getInputExpression(), transportArc.getOutputExpression(), numbExpr);
                expressionsCommand.redo();
                context.undoManager().addEdit(expressionsCommand);
            }else{
                Vector<ColorExpression> vecColorExpr = new Vector<>();
                vecColorExpr.add(colorType.createColorExpressionForFirstColor());
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                Command arcExpressionCommand = new SetArcExpressionCommand(arc,arc.getExpression(),numbExpr);
                arcExpressionCommand.redo();
                context.undoManager().addEdit(arcExpressionCommand);
            }
        }
    }

    private void writeTokensToList(TimedPlace tp) {
        coloredTokenListModel.clear();
        AddExpression tokenExpression = (AddExpression)tp.getTokensAsExpression();
        if(tokenExpression != null){
            for(ArcExpression expr : tokenExpression.getAddExpression()){
                addTokenExpression((NumberOfExpression)expr);
            }
        }
        updateSpinnerValue(true);
    }

    private void updateSpinnerValue(boolean updateSelection){
        NumberOfExpression expr = buildTokenExpression(1);

        if(coloredTokenListModel.getSize() > 0){
            for(int i = 0; i < coloredTokenListModel.getSize();i++){
                NumberOfExpression otherExpr = coloredTokenListModel.getElementAt(i);
                if(expr.equalsColor(otherExpr)){
                    addTokenSpinner.setValue(otherExpr.getNumber());
                    if(updateSelection){
                        tokenList.setSelectedIndex(i);
                    }
                    return;
                }
            }
        }
        addTokenSpinner.setValue(1);
        tokenList.clearSelection();
        addColoredTokenButton.setText("Add");
    }

    private void setInitialComboBoxValue() {
        List<ColorType> colorTypes = context.network().colorTypes();
        if (colorType != null) {
            colorTypeComboBox.setSelectedIndex(colorTypes.indexOf(colorType));
        }
        else if (colorTypes.size() != 0) {
            colorTypeComboBox.setSelectedIndex(0);
        }
    }

    private void setNewColorType(ColorType colorType) {
        this.colorType = colorType;
        coloredTokenListModel.clear();
        timeConstraintListModel.clear();
        tokenColorComboboxPanel.updateColorType(colorType);
        colorInvariantComboboxPanel.updateColorType(colorType);
        tokenColorComboboxPanel.changedColor(tokenColorComboboxPanel.getColorTypeComboBoxesArray());
        colorInvariantComboboxPanel.changedColor(colorInvariantComboboxPanel.getColorTypeComboBoxesArray());
        parent.pack();
    }

    private void setColoredTimeInvariants(TimedPlace tp) {
        timeConstraintListModel.clear();
        for (ColoredTimeInvariant timeInvariant : tp.getCtiList()) {
            timeConstraintListModel.addElement(timeInvariant);
        }
        timeConstraintList.setSelectedIndex(0);
    }

    private void addTokenExpression(NumberOfExpression expr){
	    boolean exists = false;
        for(int i = 0; i < coloredTokenListModel.getSize();i++){
            NumberOfExpression otherExpr = coloredTokenListModel.getElementAt(i);
            if(expr.equalsColor(otherExpr)){
                exists = true;

                otherExpr.setNumber(expr.getNumber());

                break;
            }
        }
        if(!exists){
            coloredTokenListModel.addElement(expr);
        }
        tokenList.updateUI();
    }

    private NumberOfExpression buildTokenExpression(int number){
        Vector<ColorExpression> exprVec = new Vector<>();
        TupleExpression tupleExpression;
        if (colorType instanceof ProductType) {
            Vector<ColorExpression> tempVec = new Vector<>();
            for (int i = 0; i < tokenColorComboboxPanel.getColorTypeComboBoxesArray().length; i++) {
                ColorExpression expr;
                if (tokenColorComboboxPanel.getColorTypeComboBoxesArray()[i].getItemAt(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[i].getSelectedIndex()) instanceof String) {
                    //We hack this
                    //"all" is always last, so we get the colortype by taking the colortype of the first element
                    expr = new AllExpression(((dk.aau.cs.model.CPN.Color) tokenColorComboboxPanel.getColorTypeComboBoxesArray()[i]
                        .getItemAt(0)).getColorType());
                } else {
                    expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) tokenColorComboboxPanel.getColorTypeComboBoxesArray()[i]
                        .getItemAt(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[i].getSelectedIndex()));
                }
                tempVec.add(expr);
            }
            tupleExpression = new TupleExpression(tempVec);
            exprVec.add(tupleExpression);
        } else {
            ColorExpression expr;
            if (tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0].getItemAt(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0].getSelectedIndex()) instanceof String) {
                expr = new AllExpression(colorType);
            } else {
                expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0]
                    .getItemAt(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0].getSelectedIndex()));
            }
            exprVec.add(expr);
        }
        return new NumberOfExpression(number, exprVec);
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
    private JPanel tokenPanel;
    private DefaultListModel<NumberOfExpression> coloredTokenListModel;
    private JList tokenList;
    private JPanel tokenButtonPanel;
    private JButton addColoredTokenButton;
    private JButton removeColoredTokenButton;
    private ColorComboboxPanel tokenColorComboboxPanel;
    private ColorType colorType;
    JPanel timeInvariantColorPanel;
    DefaultListModel<ColoredTimeInvariant> timeConstraintListModel;
    JList<ColoredTimeInvariant> timeConstraintList;
    JComboBox<ColorType> colorTypeComboBox;
    JPanel colorTypePanel;
    JSpinner addTokenSpinner;
    ColoredTimeInvariantDialogPanel invariantEditorPanel;
    JButton addTimeConstraintButton;
    JButton removeTimeConstraintButton;
    ColorComboboxPanel colorInvariantComboboxPanel;

}
