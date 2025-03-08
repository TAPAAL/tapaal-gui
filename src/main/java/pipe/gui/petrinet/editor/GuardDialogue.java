package pipe.gui.petrinet.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import dk.aau.cs.model.CPN.ColoredTimeInterval;
import net.tapaal.gui.petrinet.Context;
import net.tapaal.gui.petrinet.Template;
import dk.aau.cs.model.tapn.*;
import net.tapaal.swinghelpers.SwingHelper;
import net.tapaal.swinghelpers.WidthAdjustingComboBox;
import net.tapaal.gui.petrinet.editor.ColoredArcGuardPanel;
import pipe.gui.petrinet.graphicElements.Arc;
import pipe.gui.petrinet.graphicElements.PetriNetObject;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.graphicElements.Transition;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransportArcComponent;
import pipe.gui.petrinet.undo.UndoManager;
import dk.aau.cs.model.tapn.Bound.InfBound;

public class GuardDialogue extends JPanel 
{
	private final JRootPane myRootPane;
	private JPanel guardEditPanel;
	private JPanel weightEditPanel;
	private JPanel buttonPanel;

	private JButton okButton;
	private JButton cancelButton;

	private JLabel label;
	private JSpinner weightNumber;
	private JSpinner firstIntervalNumber;
	private JSpinner secondIntervalNumber;

	private JCheckBox inf;

	private JComboBox<String> leftDelimiter;
	private JComboBox<String> rightDelimiter;

	private JCheckBox leftUseConstant;
	private WidthAdjustingComboBox<String> leftConstantsComboBox;
	private JCheckBox rightUseConstant;
	private WidthAdjustingComboBox<String> rightConstantsComboBox;
	private JCheckBox weightUseConstant;
	private WidthAdjustingComboBox<String> weightConstantsComboBox;
	private ColoredArcGuardPanel coloredArcGuardPanel;
    final PetriNetObject objectToBeEdited;
    final Context context;
    final JPanel mainPanel;
    final JScrollPane scrollPane;
	
	private final int maxNumberOfPlacesToShowAtOnce = 20;

	public GuardDialogue(JRootPane rootPane, PetriNetObject objectToBeEdited, Context context) {
		myRootPane = rootPane;
		this.context = context;
		this.objectToBeEdited = objectToBeEdited;
		setLayout(new BorderLayout());
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(mainPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(null);

		initTimeGuardPanel();

        guardEditPanel.setVisible(objectToBeEdited.isTimed() && objectToBeEdited instanceof TimedInputArcComponent
            && !(objectToBeEdited instanceof TimedInhibitorArcComponent));
        if(objectToBeEdited instanceof TimedTransportArcComponent && ((TimedTransportArcComponent) objectToBeEdited).getSource() instanceof Transition){
            guardEditPanel.setVisible(false);
        }

		initWeightPanel();
		initButtonPanel(objectToBeEdited);
		initColoredArcPanel();

		myRootPane.setDefaultButton(okButton);
		
		if(objectToBeEdited instanceof TimedInputArcComponent && !(objectToBeEdited instanceof TimedInhibitorArcComponent)){
			setNoncoloredInitialState((TimedInputArcComponent) objectToBeEdited);
		}
		// Weights
		if(objectToBeEdited instanceof TimedOutputArcComponent){
			TimedOutputArcComponent arc = (TimedOutputArcComponent)objectToBeEdited;
			if(arc.getWeight() instanceof ConstantWeight){
				weightConstantsComboBox.setSelectedItem(((ConstantWeight)arc.getWeight()).constant().name());
				weightUseConstant.doClick();
			}
			
			weightNumber.setValue(((TimedOutputArcComponent)objectToBeEdited).getWeight().value());
		}
		add(scrollPane, BorderLayout.CENTER);
		hideIrrelevantInformation();
	}

	private void hideIrrelevantInformation(){
        if(!objectToBeEdited.isTimed()){
            if(guardEditPanel != null){
                guardEditPanel.setVisible(false);
            }
        }
        if(!objectToBeEdited.isColored() || objectToBeEdited instanceof TimedInhibitorArcComponent){
            coloredArcGuardPanel.setVisible(false);
        } else{
            if(objectToBeEdited.isTimed() && guardEditPanel != null){
                guardEditPanel.setBorder(BorderFactory.createTitledBorder("Default Time Interval"));
            }
            weightEditPanel.setVisible(false);
        }
    }

    private void initColoredArcPanel(){
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        coloredArcGuardPanel = new ColoredArcGuardPanel(objectToBeEdited, context) {
            @Override
            public void disableOkButton() {
                okButton.setEnabled(false);
            }

            @Override
            public void enableOkButton() {
                okButton.setEnabled(true);
            }
        };
	    mainPanel.add(coloredArcGuardPanel, gridBagConstraints);
    }

	private void initButtonPanel(final PetriNetObject objectToBeEdited) {
		buttonPanel = new JPanel(new GridBagLayout());

		okButton = new JButton("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		
		cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				TimedOutputArcComponent arc = (TimedOutputArcComponent) objectToBeEdited;
				UndoManager undoManager = context.undoManager();
				undoManager.newEdit();

                dk.aau.cs.model.tapn.TimeInterval guard = null;
				if(objectToBeEdited instanceof TimedInputArcComponent && !(objectToBeEdited instanceof TimedInhibitorArcComponent)){
                    guard = composeGuard(((TimedInputArcComponent)arc).getGuard());
				}
				
				// Check if target transition is urgent
				if(objectToBeEdited instanceof TimedInputArcComponent && !(objectToBeEdited instanceof TimedInhibitorArcComponent)
						&& ((TimedInputArcComponent) objectToBeEdited).isUrgentTransition()){
					if(!guard.equals(TimeInterval.ZERO_INF)){
						JOptionPane.showMessageDialog(myRootPane, "Incoming arcs to urgent transitions must have the interval [0," + '\u221E' + ")", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					} else {
					    for (Object interval : coloredArcGuardPanel.getTimeConstraintModel().toArray()) {
					        if (interval instanceof ColoredTimeInterval && !((ColoredTimeInterval) interval).getInterval().equals(TimeInterval.ZERO_INF.toString())) {
                                JOptionPane.showMessageDialog(myRootPane, "Incoming arcs to urgent transitions must have the interval [0," + '\u221E' + ")", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
				}
				//Update colors
				coloredArcGuardPanel.onOkColored(undoManager);
				Weight weight = composeWeight();
				
                boolean sharedSrcDst = false;
                String sourceName = "", targetName = "";
                
                if (objectToBeEdited instanceof TimedInputArcComponent || objectToBeEdited instanceof TimedInhibitorArcComponent) {
                    TimedPlaceComponent sourcePlace = (TimedPlaceComponent)arc.getSource();
                    TimedTransitionComponent targetTransition = (TimedTransitionComponent)arc.getTarget();
                    
                    boolean isSourceShared = sourcePlace.underlyingPlace().isShared();
                    boolean isTargetShared = targetTransition.underlyingTransition().isShared();
                    sharedSrcDst = isSourceShared && isTargetShared;
                    
                    if (sharedSrcDst) {
                        sourceName = sourcePlace.getName();
                        targetName = targetTransition.getName();
                    }
                } else if (!(objectToBeEdited instanceof TimedTransportArcComponent)) {
                    TimedTransitionComponent sourceTransition = (TimedTransitionComponent)arc.getSource();
                    TimedPlaceComponent targetPlace = (TimedPlaceComponent)arc.getTarget();
                    
                    boolean isSourceShared = sourceTransition.underlyingTransition().isShared();
                    boolean isTargetShared = targetPlace.underlyingPlace().isShared();
                    sharedSrcDst = isSourceShared && isTargetShared;
                    
                    if (sharedSrcDst) {
                        sourceName = sourceTransition.getName();
                        targetName = targetPlace.getName();
                    }
                } else if (objectToBeEdited instanceof TimedTransportArcComponent) {
                    TimedTransportArcComponent transportArc = (TimedTransportArcComponent)objectToBeEdited;
                    TimedPlace sourcePlace = transportArc.underlyingTransportArc().source();
                    TimedTransition transition = transportArc.underlyingTransportArc().transition();
                    TimedPlace targetPlace = transportArc.underlyingTransportArc().destination();
                    
                    boolean isSourceShared = sourcePlace.isShared();
                    boolean isTransitionShared = transition.isShared();
                    boolean isTargetShared = targetPlace.isShared();
                    
                    sharedSrcDst = isSourceShared && isTransitionShared && isTargetShared;
                }
                
                if (sharedSrcDst) {
                    for (Template template : context.tabContent().allTemplates()) {
                        TimedOutputArcComponent templateArc = null;
                        if (objectToBeEdited instanceof TimedInputArcComponent || objectToBeEdited instanceof TimedInhibitorArcComponent) {
                            PlaceTransitionObject templateSource = template.guiModel().getPlaceByName(sourceName);
                            PlaceTransitionObject templateTarget = template.guiModel().getTransitionByName(targetName);
                            
                            if (templateSource != null && templateTarget != null) {
                                // Find matching arc
                                for (Arc possibleArc : templateSource.getPostset()) {
                                    if (possibleArc.getTarget().equals(templateTarget) &&
                                        ((objectToBeEdited instanceof TimedInhibitorArcComponent && possibleArc instanceof TimedInhibitorArcComponent) ||
                                        (objectToBeEdited instanceof TimedInputArcComponent && !(objectToBeEdited instanceof TimedInhibitorArcComponent) && possibleArc instanceof TimedInputArcComponent))) {
                                        templateArc = (TimedOutputArcComponent)possibleArc;
                                        break;
                                    }
                                }
                            }
                        } else if (!(objectToBeEdited instanceof TimedTransportArcComponent)) {
                            PlaceTransitionObject templateSource = template.guiModel().getTransitionByName(sourceName);
                            PlaceTransitionObject templateTarget = template.guiModel().getPlaceByName(targetName);
                            
                            if (templateSource != null && templateTarget != null) {
                                // Find matching arc
                                for (Arc possibleArc : templateSource.getPostset()) {
                                    if (possibleArc.getTarget().equals(templateTarget) && possibleArc instanceof TimedOutputArcComponent) {
                                        templateArc = (TimedOutputArcComponent)possibleArc;
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if (templateArc != null) {
                            undoManager.addEdit(templateArc.setGuardAndWeight(guard, weight));
                        }
                    }
                } else {
                    undoManager.addEdit(arc.setGuardAndWeight(guard, weight));
                }

				context.network().buildConstraints();
				exit();
			}
			
			private Weight composeWeight(){
				
				Weight weight;
				
				if(weightUseConstant.isSelected()){
					String constantName = weightConstantsComboBox.getSelectedItem().toString();
					weight = new ConstantWeight(context.network().getConstant(constantName));
				} else {
					weight = new IntWeight((Integer) weightNumber.getValue());
				}
				
				return weight;
			}

			private dk.aau.cs.model.tapn.TimeInterval composeGuard(
					dk.aau.cs.model.tapn.TimeInterval oldGuard) {
				boolean useConstantLeft = leftUseConstant.isSelected();
				boolean useConstantRight = rightUseConstant.isSelected();

				String leftDelim = leftDelimiter.getSelectedItem().toString();
				String rightDelim = rightDelimiter.getSelectedItem().toString();
				Bound leftInterval = null;
				Bound rightInterval = null;

				if (useConstantLeft) {
					String constantName = leftConstantsComboBox.getSelectedItem().toString();
					leftInterval = new ConstantBound(context.network().getConstant(constantName));
				} else
					leftInterval = new IntBound((Integer) firstIntervalNumber.getValue());

				if (useConstantRight) {
					String constantName = rightConstantsComboBox.getSelectedItem().toString();
					rightInterval = new ConstantBound(context.network().getConstant(constantName));
				} else if (inf.isSelected()) {
                    rightInterval = Bound.Infinity;
                } else {
                    rightInterval = new IntBound((Integer) secondIntervalNumber.getValue());
                }

				if (rightInterval instanceof InfBound
						|| leftInterval.value() <= rightInterval.value()) {
					return new dk.aau.cs.model.tapn.TimeInterval(
							(leftDelim.equals("[")), leftInterval,
							rightInterval, (rightDelim.equals("]")));
				} else {
					return oldGuard;
				}
			}
		});
		cancelButton.addActionListener(evt -> exit());

	
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		buttonPanel.add(cancelButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		buttonPanel.add(okButton, gridBagConstraints);

		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(0, 0, 5, 0);
		mainPanel.add(buttonPanel, gridBagConstraints);
	}
	
	private void initWeightPanel() {
		weightEditPanel = new JPanel(new GridBagLayout());
		weightEditPanel.setBorder(BorderFactory.createTitledBorder("Weight"));
		
		label = new JLabel("Weight:");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		weightEditPanel.add(label, gridBagConstraints);

		Dimension intervalBoxDims = new Dimension(190, 25);

		weightNumber = new JSpinner();
		SwingHelper.setPreferredWidth(weightNumber,intervalBoxDims.width);
		weightNumber.addChangeListener(e -> {
			if((Integer) weightNumber.getValue() < 1){
				weightNumber.setValue(1);
			}
		});
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		weightEditPanel.add(weightNumber, gridBagConstraints);
		

		Set<String> constants = context.network().getConstantNames();
		ArrayList<String> filteredConstants = new ArrayList<String>();
		for(String constant : constants){
			if(context.network().getConstantValue(constant) != 0){
				filteredConstants.add(constant);
			}
		}
		
		
		String[] constantArray = filteredConstants.toArray(new String[0]);
		
		
	    Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);
	    
	    weightConstantsComboBox = new WidthAdjustingComboBox<>(maxNumberOfPlacesToShowAtOnce);
		weightConstantsComboBox.setModel(new DefaultComboBoxModel<>(constantArray));
		weightConstantsComboBox.setMaximumRowCount(20);
		weightConstantsComboBox.setVisible(false);
        SwingHelper.setPreferredWidth(weightConstantsComboBox,intervalBoxDims.width);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		weightEditPanel.add(weightConstantsComboBox, gridBagConstraints);
		
		
		boolean enableConstantsCheckBoxes = !filteredConstants.isEmpty();
		weightUseConstant = new JCheckBox("Use Constant");
		weightUseConstant.setEnabled(enableConstantsCheckBoxes);
		weightUseConstant.addActionListener(e -> {
			weightConstantsComboBox.setVisible(weightUseConstant.isSelected());
			weightNumber.setVisible(!weightUseConstant.isSelected());
			repackIfWindow();
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		weightEditPanel.add(weightUseConstant, gridBagConstraints);
		
		// hack to ensure the content stays on the left
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		weightEditPanel.add(new JLabel(""), gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		mainPanel.add(weightEditPanel, gridBagConstraints);
	}

	private void initTimeGuardPanel() {
		guardEditPanel = new JPanel(new GridBagLayout());
		guardEditPanel
		.setBorder(BorderFactory.createTitledBorder("Time Guard"));

		label = new JLabel("Time Interval:");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		guardEditPanel.add(label, gridBagConstraints);

		String[] left = { "[", "(" };
		leftDelimiter = new JComboBox<>();
		Dimension dims = new Dimension(55, 25);
		leftDelimiter.setPreferredSize(dims);
		leftDelimiter.setMinimumSize(dims);
		leftDelimiter.setMaximumSize(dims);
		leftDelimiter.setModel(new DefaultComboBoxModel<>(left));
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(leftDelimiter, gridBagConstraints);

		String[] right = { "]", ")" };
		rightDelimiter = new JComboBox<>();
		rightDelimiter.setPreferredSize(dims);
		rightDelimiter.setMinimumSize(dims);
		rightDelimiter.setMaximumSize(dims);
		rightDelimiter.setModel(new DefaultComboBoxModel<>(right));
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(rightDelimiter, gridBagConstraints);

		inf = new JCheckBox(Character.toString('\u221e'), true);
		inf.addActionListener(evt -> {
			if (inf.isSelected()) {
				secondIntervalNumber.setEnabled(false);
				rightDelimiter.setEnabled(false);
			} else {
				secondIntervalNumber.setEnabled(true);
				rightDelimiter.setEnabled(true);
			}
			setDelimiterModels();
		});
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(inf, gridBagConstraints);

		initNonColoredTimeIntervalControls();

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		mainPanel.add(guardEditPanel, gridBagConstraints);
	}

	private void initNonColoredTimeIntervalControls() {

		Dimension intervalBoxDims = new Dimension(190, 25);

		firstIntervalNumber = new JSpinner();
        SwingHelper.setPreferredWidth(firstIntervalNumber,intervalBoxDims.width);
		firstIntervalNumber.addChangeListener(this::firstSpinnerStateChanged);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		guardEditPanel.add(firstIntervalNumber, gridBagConstraints);

		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(new JLabel(" , "), gridBagConstraints);

		secondIntervalNumber = new JSpinner();

        SwingHelper.setPreferredWidth(secondIntervalNumber,intervalBoxDims.width);
		secondIntervalNumber.addChangeListener(this::secondSpinnerStateChanged);

		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(secondIntervalNumber, gridBagConstraints);

		Set<String> constants = context.network().getConstantNames();
		String[] constantArray = constants.toArray(new String[0]);
	    Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);
		
		
		boolean enableConstantsCheckBoxes = !constants.isEmpty();
		leftUseConstant = new JCheckBox("Use Constant                    ");
		leftUseConstant.setEnabled(enableConstantsCheckBoxes);
		leftUseConstant.addActionListener(e -> {
			updateLeftComponents();
			updateRightConstantComboBox();
			setDelimiterModels();
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(leftUseConstant, gridBagConstraints);

	
		leftConstantsComboBox = new WidthAdjustingComboBox<>(maxNumberOfPlacesToShowAtOnce);
		leftConstantsComboBox.setModel(new DefaultComboBoxModel<>(constantArray));

		leftConstantsComboBox.setMaximumRowCount(20);
		leftConstantsComboBox.setVisible(false);

        SwingHelper.setPreferredWidth(leftConstantsComboBox,intervalBoxDims.width);
		leftConstantsComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				updateRightConstantComboBox();
				setDelimiterModels();
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(leftConstantsComboBox, gridBagConstraints);

		rightUseConstant = new JCheckBox("Use Constant                    ");
		rightUseConstant.setEnabled(enableConstantsCheckBoxes);
		rightUseConstant.addActionListener(e -> {
			updateRightComponents();
			updateRightConstantComboBox();
			setDelimiterModels();
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(rightUseConstant, gridBagConstraints);

		rightConstantsComboBox = new WidthAdjustingComboBox<>(maxNumberOfPlacesToShowAtOnce);
		rightConstantsComboBox.setModel(new DefaultComboBoxModel<>(constantArray));
		rightConstantsComboBox.setMaximumRowCount(20);
		rightConstantsComboBox.setVisible(false);

        SwingHelper.setPreferredWidth(rightConstantsComboBox,intervalBoxDims.width);
		gridBagConstraints = new GridBagConstraints();
		rightConstantsComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setDelimiterModels();
			}
		});

		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(rightConstantsComboBox, gridBagConstraints);
	}

	private void setNoncoloredInitialState(TimedInputArcComponent arc) {
		String timeInterval = arc.getGuardAsString();

		String[] partedTimeInterval = timeInterval.split(",");
		String firstNumber = partedTimeInterval[0].substring(1, partedTimeInterval[0].length());
		String secondNumber = partedTimeInterval[1].substring(0,
				partedTimeInterval[1].length() - 1);
		int first = 0, second = 0;
		boolean firstIsNumber = true, secondIsNumber = true;

		try {
			first = Integer.parseInt(firstNumber);
		} catch (NumberFormatException e) {
			firstIsNumber = false;
		}

		try {
			second = Integer.parseInt(secondNumber);
		} catch (NumberFormatException e) {
			secondIsNumber = false;
		}

		SpinnerNumberModel spinnerModelForFirstNumber = new SpinnerNumberModel(
				first, 0, Integer.MAX_VALUE, 1);

		SpinnerNumberModel spinnerModelForSecondNumber = null;
		boolean isInf = secondNumber.equals("inf");
		if (isInf) {
			inf.setSelected(true);
			secondIntervalNumber.setEnabled(false);
			rightDelimiter.setEnabled(false);
			spinnerModelForSecondNumber = new SpinnerNumberModel(0, 0,
					Integer.MAX_VALUE, 1);
		} else {
			inf.setSelected(false);
			spinnerModelForSecondNumber = new SpinnerNumberModel(second, 0,
					Integer.MAX_VALUE, 1);
		}
		firstIntervalNumber.setModel(spinnerModelForFirstNumber);
		secondIntervalNumber.setModel(spinnerModelForSecondNumber);

		if (!firstIsNumber) {
			leftUseConstant.setSelected(true);
			leftConstantsComboBox.setSelectedItem(firstNumber);
			updateLeftComponents();
		}

		if (!secondIsNumber && !isInf) {
			rightUseConstant.setSelected(true);
			rightConstantsComboBox.setSelectedItem(secondNumber);
			updateRightComponents();
		}

		boolean canUseConstants = rightUseConstant.isEnabled();
		if (canUseConstants) {
			updateRightConstantComboBox();
		}

		setDelimiterModels();
		if (timeInterval.contains("[")) {
			leftDelimiter.setSelectedItem("[");
		} else {
			leftDelimiter.setSelectedItem("(");
		}
		if (timeInterval.contains("]")) {
			rightDelimiter.setSelectedItem("]");
		} else {
			rightDelimiter.setSelectedItem(")");
		}
	}

	private void updateLeftComponents() {
		boolean value = leftUseConstant.isSelected();
		firstIntervalNumber.setVisible(!value);
		leftConstantsComboBox.setVisible(value);
		setDelimiterModels();
	}

	private void updateRightComponents() {
		boolean value = rightUseConstant.isSelected();
		inf.setVisible(!value);
		if (value)
			rightDelimiter.setEnabled(true);
		else
			rightDelimiter.setEnabled(!inf.isSelected());
        secondIntervalNumber.setVisible(!value);
		rightConstantsComboBox.setVisible(value);

		repackIfWindow();
		setDelimiterModels();
	}

	public void exit() {
		myRootPane.getParent().setVisible(false);
	}

	private void setDelimiterModels() {
		int firstValue = getFirstValue();
		int secondValue = getSecondValue();

		DefaultComboBoxModel<String> modelRightIncludedOnly = new DefaultComboBoxModel<>(new String[] { "]" });
		DefaultComboBoxModel<String> modelLeftIncludedOnly = new DefaultComboBoxModel<>(new String[] { "[" });
		DefaultComboBoxModel<String> modelRightBoth = new DefaultComboBoxModel<>(new String[] { "]", ")" });
		DefaultComboBoxModel<String> modelLeftBoth = new DefaultComboBoxModel<>(new String[] { "[", "(" });
		DefaultComboBoxModel<String> modelRightExcludedOnly = new DefaultComboBoxModel<>(new String[] { ")" });

		if (firstValue > secondValue) {
			secondIntervalNumber.setValue(firstValue);
			secondValue = firstValue;
		}

		String leftOldDelim = leftDelimiter.getSelectedItem().toString();
		String rightOldDelim = rightDelimiter.getSelectedItem().toString();

		if (firstValue == secondValue) {
			rightDelimiter.setModel(modelRightIncludedOnly);
			leftDelimiter.setModel(modelLeftIncludedOnly);
		} else {
			leftDelimiter.setModel(modelLeftBoth);

			if (inf.isSelected() && !rightUseConstant.isSelected())
				rightDelimiter.setModel(modelRightExcludedOnly);
			else
				rightDelimiter.setModel(modelRightBoth);
		}

		leftDelimiter.setSelectedItem(leftOldDelim);
		if (rightUseConstant.isSelected())
			rightDelimiter.setSelectedItem("]");
		else
			rightDelimiter.setSelectedItem(rightOldDelim);
	}

	private int getSecondValue() {
		int secondValue;
		if (rightUseConstant.isSelected()) {
			secondValue = context.network().getConstantValue(rightConstantsComboBox.getSelectedItem().toString());
		} else if (inf.isSelected()) {
			secondValue = Integer.MAX_VALUE;
		} else {
			secondValue = Integer.parseInt(String.valueOf(secondIntervalNumber.getValue()));
		}
		return secondValue;
	}

	private int getFirstValue() {
		int firstValue;
		if (leftUseConstant.isSelected()) {
			firstValue = context.network().getConstantValue(leftConstantsComboBox.getSelectedItem().toString());
		} else {
			firstValue = Integer.parseInt(String.valueOf(firstIntervalNumber.getValue()));
		}
		return firstValue;
	}
	
	private void updateWeightConstantComboBox() {
		int value = getFirstValue();

		String oldWeight = weightConstantsComboBox.getSelectedItem() != null ? weightConstantsComboBox
				.getSelectedItem().toString()
				: null;
				weightConstantsComboBox.removeAllItems();
				Collection<Constant> constants = context.network().constants();

				//List <Constant> constantList = new ArrayList(constants);
				List <Constant> constantList = new ArrayList<Constant>();
				constantList.addAll(constants);

				constantList.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));

				for (Constant c : constantList) {
					if (c.value() >= value) {
						weightConstantsComboBox.addItem(c.name());
					}
				}

				if (oldWeight != null) {
                    weightConstantsComboBox.setSelectedItem(oldWeight);
                }
	}

	private void updateRightConstantComboBox() {
		int value = getFirstValue();

		String oldRight = rightConstantsComboBox.getSelectedItem() != null ? rightConstantsComboBox
				.getSelectedItem().toString()
				: null;
        rightConstantsComboBox.removeAllItems();
        Collection<Constant> constants = context.network().constants();

        //List <Constant> constantList = new ArrayList(constants);
        List <Constant> constantList = new ArrayList<Constant>();
        constantList.addAll(constants);

        constantList.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));


        for (Constant c : constantList) {
            if (c.value() >= value) {
                rightConstantsComboBox.addItem(c.name());
            }
        }

        if(rightConstantsComboBox.getItemCount() == 0){
            rightUseConstant.setEnabled(false);
            rightUseConstant.setSelected(false);
            updateRightComponents();
        } else {
            rightUseConstant.setEnabled(true);
        }

        if (oldRight != null)
            rightConstantsComboBox.setSelectedItem(oldRight);
	}

	private void firstSpinnerStateChanged(ChangeEvent evt) {
		int firstValue = getFirstValue();
		int secondValue = getSecondValue();
		if (rightUseConstant.isSelected() && firstValue > secondValue) {
			rightUseConstant.setSelected(false);
			updateRightComponents();
		}
		if (firstValue > context.network()
				.getLargestConstantValue())
			rightUseConstant.setEnabled(false);
		else {
			rightUseConstant.setEnabled(true);
			updateRightConstantComboBox();
		}
		setDelimiterModels();
	}

	private void secondSpinnerStateChanged(ChangeEvent evt) {
		setDelimiterModels();
	}
	
	private void repackIfWindow() {
		if(myRootPane.getParent() instanceof Window){
			((Window)myRootPane.getParent()).pack();
		}
	}
}
