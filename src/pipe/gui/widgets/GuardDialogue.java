package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.ConstantWeight;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.IntWeight;
import dk.aau.cs.model.tapn.Weight;

public class GuardDialogue extends JPanel /*
 * implements ActionListener,
 * PropertyChangeListener
 */
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4582651236913407101L;
	private JRootPane myRootPane;
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

	private JComboBox leftDelimiter;
	private JComboBox rightDelimiter;

	private JCheckBox leftUseConstant;
	private WidthAdjustingComboBox leftConstantsComboBox;
	private JCheckBox rightUseConstant;
	private WidthAdjustingComboBox rightConstantsComboBox;
	private JCheckBox weightUseConstant;
	private WidthAdjustingComboBox weightConstantsComboBox;
	
	private int maxNumberOfPlacesToShowAtOnce = 20;

	public GuardDialogue(JRootPane rootPane, PetriNetObject objectToBeEdited) {
		myRootPane = rootPane;
		setLayout(new GridBagLayout());

		if(objectToBeEdited instanceof TimedInputArcComponent && !(objectToBeEdited instanceof TimedInhibitorArcComponent)){
			initTimeGuardPanel();
		}
		
		initWeightPanel();
		initButtonPanel(objectToBeEdited);

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
				UndoManager undoManager = CreateGui.getDrawingSurface().getUndoManager();
				undoManager.newEdit();

				dk.aau.cs.model.tapn.TimeInterval guard  = null;
				if(objectToBeEdited instanceof TimedInputArcComponent && !(objectToBeEdited instanceof TimedInhibitorArcComponent)){
					guard = composeGuard(arc.getGuard());
				}
				
				// Check if target transition is urgent
				if(objectToBeEdited instanceof TimedInputArcComponent && !(objectToBeEdited instanceof TimedInhibitorArcComponent)
						&& ((TimedInputArcComponent) objectToBeEdited).isUrgentTransition()){
					if(!guard.equals(guard.ZERO_INF)){
						JOptionPane.showMessageDialog(myRootPane, "Incoming arcs to urgent transitions must have the interval [0,inf).", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				
				Weight weight = composeWeight();
				undoManager.addEdit(arc.setGuardAndWeight(guard, weight));
				CreateGui.getCurrentTab().network().buildConstraints();
				exit();
			}
			
			private Weight composeWeight(){
				
				Weight weight;
				
				if(weightUseConstant.isSelected()){
					String constantName = weightConstantsComboBox
							.getSelectedItem().toString();
					weight = new ConstantWeight(CreateGui.getCurrentTab().network().getConstant(constantName));
				} else {
					weight = new IntWeight(((Integer)weightNumber.getValue()).intValue());
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
					String constantName = leftConstantsComboBox
					.getSelectedItem().toString();
					leftInterval = new ConstantBound(CreateGui.getCurrentTab()
							.network().getConstant(constantName));
				} else
					leftInterval = new IntBound((Integer) firstIntervalNumber
							.getValue());

				if (useConstantRight) {
					String constantName = rightConstantsComboBox
					.getSelectedItem().toString();
					rightInterval = new ConstantBound(CreateGui.getCurrentTab()
							.network().getConstant(constantName));
				} else if (inf.isSelected())
					rightInterval = Bound.Infinity;
				else
					rightInterval = new IntBound((Integer) secondIntervalNumber
							.getValue());

				if (rightInterval instanceof InfBound
						|| leftInterval.value() <= rightInterval.value()) {
					return new dk.aau.cs.model.tapn.TimeInterval(
							(leftDelim.equals("[") ? true : false), leftInterval,
							rightInterval, (rightDelim.equals("]") ? true : false));
				} else {
					return oldGuard;
				}
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exit();
			}
		});

	
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
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(0, 0, 5, 0);
		add(buttonPanel, gridBagConstraints);
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
		weightNumber.setPreferredSize(intervalBoxDims);
		weightNumber.addChangeListener(new ChangeListener() {
			
			
			public void stateChanged(ChangeEvent e) {
				if((Integer) weightNumber.getValue() < 1){
					weightNumber.setValue(1);
				}
			}
		});
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		weightEditPanel.add(weightNumber, gridBagConstraints);
		

		Set<String> constants = CreateGui.getCurrentTab().network()
		.getConstantNames();
		ArrayList<String> filteredConstants = new ArrayList<String>();
		for(String constant : constants){
			if(CreateGui.getCurrentTab().network().getConstantValue(constant) != 0){
				filteredConstants.add(constant);
			}
		}
		
		
		String[] constantArray = filteredConstants.toArray(new String[filteredConstants.size()]);
		
		
	    Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);
	    
	    weightConstantsComboBox = new WidthAdjustingComboBox(maxNumberOfPlacesToShowAtOnce);
		weightConstantsComboBox.setModel(new DefaultComboBoxModel(constantArray));
		weightConstantsComboBox.setMaximumRowCount(20);
		weightConstantsComboBox.setVisible(false);
		weightConstantsComboBox.setPreferredSize(intervalBoxDims);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		weightEditPanel.add(weightConstantsComboBox, gridBagConstraints);
		
		
		boolean enableConstantsCheckBoxes = !filteredConstants.isEmpty();
		weightUseConstant = new JCheckBox("Use Constant");
		weightUseConstant.setEnabled(enableConstantsCheckBoxes);
		weightUseConstant.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				weightConstantsComboBox.setVisible(weightUseConstant.isSelected());
				weightNumber.setVisible(!weightUseConstant.isSelected());
				repackIfWindow();
			}
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
		add(weightEditPanel, gridBagConstraints);
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
		leftDelimiter = new JComboBox();
		Dimension dims = new Dimension(55, 25);
		leftDelimiter.setPreferredSize(dims);
		leftDelimiter.setMinimumSize(dims);
		leftDelimiter.setMaximumSize(dims);
		leftDelimiter.setModel(new DefaultComboBoxModel(left));
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(leftDelimiter, gridBagConstraints);

		String[] right = { "]", ")" };
		rightDelimiter = new JComboBox();
		rightDelimiter.setPreferredSize(dims);
		rightDelimiter.setMinimumSize(dims);
		rightDelimiter.setMaximumSize(dims);
		rightDelimiter.setModel(new DefaultComboBoxModel(right));
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(rightDelimiter, gridBagConstraints);

		inf = new JCheckBox("inf", true);
		inf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (inf.isSelected()) {
					secondIntervalNumber.setEnabled(false);
					rightDelimiter.setEnabled(false);
				} else {
					secondIntervalNumber.setEnabled(true);
					rightDelimiter.setEnabled(true);
				}
				setDelimiterModels();
			}
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
		add(guardEditPanel, gridBagConstraints);
	}

	private void initNonColoredTimeIntervalControls() {

		Dimension intervalBoxDims = new Dimension(190, 25);

		firstIntervalNumber = new JSpinner();
	//	firstIntervalNumber.setMaximumSize(intervalBoxDims);
	//	firstIntervalNumber.setMinimumSize(intervalBoxDims);
		firstIntervalNumber.setPreferredSize(intervalBoxDims);
		firstIntervalNumber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				firstSpinnerStateChanged(evt);
			}
		});
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
		secondIntervalNumber.setMaximumSize(intervalBoxDims);
		secondIntervalNumber.setMinimumSize(intervalBoxDims);
		secondIntervalNumber.setPreferredSize(intervalBoxDims);
		secondIntervalNumber.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				secondSpinnerStateChanged(evt);
			}
		});

		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(secondIntervalNumber, gridBagConstraints);

		Set<String> constants = CreateGui.getCurrentTab().network()
		.getConstantNames();
		String[] constantArray = constants.toArray(new String[constants.size()]);
	    Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);
		
		
		boolean enableConstantsCheckBoxes = !constants.isEmpty();
		leftUseConstant = new JCheckBox("Use Constant                    ");
		leftUseConstant.setEnabled(enableConstantsCheckBoxes);
		leftUseConstant.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLeftComponents();
				updateRightConstantComboBox();
				setDelimiterModels();
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(leftUseConstant, gridBagConstraints);

	
		leftConstantsComboBox = new WidthAdjustingComboBox(maxNumberOfPlacesToShowAtOnce);
		leftConstantsComboBox.setModel(new DefaultComboBoxModel(constantArray));
	//	leftConstantsComboBox = new JComboBox(constants.toArray());
		leftConstantsComboBox.setMaximumRowCount(20);
		leftConstantsComboBox.setVisible(false);
	//	leftConstantsComboBox.setMaximumSize(intervalBoxDims);
	//  leftConstantsComboBox.setMinimumSize(intervalBoxDims);
		leftConstantsComboBox.setPreferredSize(intervalBoxDims);
		leftConstantsComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateRightConstantComboBox();
					setDelimiterModels();
				}
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(leftConstantsComboBox, gridBagConstraints);

		rightUseConstant = new JCheckBox("Use Constant                    ");
		rightUseConstant.setEnabled(enableConstantsCheckBoxes);
		rightUseConstant.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateRightComponents();
				updateRightConstantComboBox();
				setDelimiterModels();
			}
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		guardEditPanel.add(rightUseConstant, gridBagConstraints);

		rightConstantsComboBox = new WidthAdjustingComboBox(maxNumberOfPlacesToShowAtOnce);
		rightConstantsComboBox.setModel(new DefaultComboBoxModel(constantArray));
		rightConstantsComboBox.setMaximumRowCount(20);
		rightConstantsComboBox.setVisible(false);
	//	rightConstantsComboBox.setMaximumSize(intervalBoxDims);
	//	rightConstantsComboBox.setMinimumSize(intervalBoxDims);
		rightConstantsComboBox.setPreferredSize(intervalBoxDims);
		gridBagConstraints = new GridBagConstraints();
		rightConstantsComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setDelimiterModels();
				}
			}
		});

		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(rightConstantsComboBox, gridBagConstraints);
	}

	private void setNoncoloredInitialState(TimedInputArcComponent arc) {
		String timeInterval = arc.getGuardAsString();

		String[] partedTimeInterval = timeInterval.split(",");
		String firstNumber = partedTimeInterval[0].substring(1,
				partedTimeInterval[0].length());
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

		DefaultComboBoxModel modelRightIncludedOnly = new DefaultComboBoxModel(
				new String[] { "]" });
		DefaultComboBoxModel modelLeftIncludedOnly = new DefaultComboBoxModel(
				new String[] { "[" });
		DefaultComboBoxModel modelRightBoth = new DefaultComboBoxModel(
				new String[] { "]", ")" });
		DefaultComboBoxModel modelLeftBoth = new DefaultComboBoxModel(
				new String[] { "[", "(" });
		DefaultComboBoxModel modelRightExcludedOnly = new DefaultComboBoxModel(
				new String[] { ")" });

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
			secondValue = CreateGui.getCurrentTab().network().getConstantValue(
					rightConstantsComboBox.getSelectedItem().toString());
		} else if (inf.isSelected()) {
			secondValue = Integer.MAX_VALUE;
		} else {
			secondValue = Integer.parseInt(String.valueOf(secondIntervalNumber
					.getValue()));
		}
		return secondValue;
	}

	private int getFirstValue() {
		int firstValue;
		if (leftUseConstant.isSelected()) {
			firstValue = CreateGui.getCurrentTab().network().getConstantValue(
					leftConstantsComboBox.getSelectedItem().toString());
		} else {
			firstValue = Integer.parseInt(String.valueOf(firstIntervalNumber
					.getValue()));
		}
		return firstValue;
	}
	
	private void updateWeightConstantComboBox() {
		int value = getFirstValue();

		String oldWeight = weightConstantsComboBox.getSelectedItem() != null ? weightConstantsComboBox
				.getSelectedItem().toString()
				: null;
				weightConstantsComboBox.removeAllItems();
				Collection<Constant> constants = CreateGui.getCurrentTab().network()
						.constants();

				//List <Constant> constantList = new ArrayList(constants);
				List <Constant> constantList = new ArrayList<Constant>();
				constantList.addAll(constants);

				Collections.sort(constantList,new Comparator<Constant>() {
					public int compare(Constant o1, Constant o2) {
						return o1.name().compareToIgnoreCase(o2.name());
					}
				});


				for (Constant c : constantList) {
					if (c.value() >= value) {
						weightConstantsComboBox.addItem(c.name());
					}
				}

				// if(rightConstantsComboBox.getItemCount() == 0){
				// rightUseConstant.setEnabled(false);
				// }

				if (oldWeight != null)
					weightConstantsComboBox.setSelectedItem(oldWeight);
	}

	private void updateRightConstantComboBox() {
		int value = getFirstValue();

		String oldRight = rightConstantsComboBox.getSelectedItem() != null ? rightConstantsComboBox
				.getSelectedItem().toString()
				: null;
				rightConstantsComboBox.removeAllItems();
				Collection<Constant> constants = CreateGui.getCurrentTab().network()
				.constants();
				
				//List <Constant> constantList = new ArrayList(constants);
				List <Constant> constantList = new ArrayList<Constant>();
				constantList.addAll(constants);
				
				Collections.sort(constantList,new Comparator<Constant>() {
						public int compare(Constant o1, Constant o2) {
						return o1.name().compareToIgnoreCase(o2.name());
						}
				});

				
				for (Constant c : constantList) {
					if (c.value() >= value) {
						rightConstantsComboBox.addItem(c.name());
					}
				}

				if(rightConstantsComboBox.getItemCount() == 0){
					rightUseConstant.setEnabled(false);
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
		if (firstValue > CreateGui.getCurrentTab().network()
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
