package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedTransitionComponent;
import pipe.dataLayer.TransportArcComponent;
import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredInhibitorArc;
import pipe.dataLayer.colors.ColoredInputArc;
import pipe.dataLayer.colors.ColoredInterval;
import pipe.dataLayer.colors.ColoredTransportArc;
import pipe.dataLayer.colors.IntOrConstant;
import pipe.dataLayer.colors.IntOrConstantRange;
import pipe.dataLayer.colors.IntervalBound;
import pipe.dataLayer.colors.Preserve;
import pipe.gui.CreateGui;
import pipe.gui.undo.UndoManager;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.IntBound;
import dk.aau.cs.model.tapn.Bound.InfBound;

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
	private JPanel colorGuardPanel;
	private JPanel transportArcFeaturesPanel;
	private JPanel buttonPanel;

	private JRadioButton preserveAgeRadioBtn;
	private JRadioButton preserveValueRadioBtn;
	private JRadioButton preserveBothRadioBtn;
	private JLabel updateExprLabel;
	private JTextField updateExprTextbox;

	private JButton okButton;
	private JButton cancelButton;

	private JLabel label;
	private JSpinner firstIntervalNumber;
	private JSpinner secondIntervalNumber;

	private JCheckBox inf;

	private JComboBox leftDelimiter;
	private JComboBox rightDelimiter;

	private JCheckBox leftUseConstant;
	private JComboBox leftConstantsComboBox;
	private JCheckBox rightUseConstant;
	private JComboBox rightConstantsComboBox;

	private JLabel colorGuardLabel;
	private JTextField colorGuardTextBox;
	private JLabel colorExampleLabel;
	private JTextField lowerScaleTextbox;
	private JTextField lowerOffsetTextbox;
	private JTextField upperScaleTextbox;
	private JTextField upperOffsetTextbox;

	public GuardDialogue(JRootPane rootPane, PetriNetObject objectToBeEdited) {
		myRootPane = rootPane;
		setLayout(new GridBagLayout());

		initTimeGuardPanel();

		if (CreateGui.getModel().isUsingColors()) {
			initColorGuardPanel();
			if (objectToBeEdited instanceof TransportArcComponent) {
				initTransportArcFeaturesPanel();
				TransportArcComponent arc = (TransportArcComponent) objectToBeEdited;
				if (arc.getConnectedTo() != null
						&& arc.getSource() instanceof TimedTransitionComponent) {
					objectToBeEdited = arc.getConnectedTo();
				}
			}
		}
		initButtonPanel(objectToBeEdited);

		myRootPane.setDefaultButton(okButton);

		setNoncoloredInitialState((TimedInputArcComponent) objectToBeEdited);
	}


	private void initTransportArcFeaturesPanel() {
		transportArcFeaturesPanel = new JPanel(new GridBagLayout());
		transportArcFeaturesPanel.setBorder(BorderFactory
				.createTitledBorder("Transport Arc Features"));

		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (preserveAgeRadioBtn.isSelected()) {
					updateExprTextbox.setEnabled(true);
				} else {
					updateExprTextbox.setEnabled(false);
				}
			}
		};

		preserveBothRadioBtn = new JRadioButton("Preserve age and value");
		preserveBothRadioBtn.addActionListener(listener);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		transportArcFeaturesPanel.add(preserveBothRadioBtn, gridBagConstraints);

		preserveAgeRadioBtn = new JRadioButton("Preserve age");
		preserveAgeRadioBtn.addActionListener(listener);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		transportArcFeaturesPanel.add(preserveAgeRadioBtn, gridBagConstraints);

		preserveValueRadioBtn = new JRadioButton("Preserve value");
		preserveValueRadioBtn.addActionListener(listener);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		transportArcFeaturesPanel
				.add(preserveValueRadioBtn, gridBagConstraints);

		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(preserveBothRadioBtn);
		btnGroup.add(preserveAgeRadioBtn);
		btnGroup.add(preserveValueRadioBtn);

		updateExprLabel = new JLabel("Update value:");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		transportArcFeaturesPanel.add(updateExprLabel, gridBagConstraints);

		updateExprTextbox = new JTextField();
		updateExprTextbox.setEnabled(false);
		Dimension intervalBoxDims = new Dimension(90, 25);
		updateExprTextbox.setMaximumSize(intervalBoxDims);
		updateExprTextbox.setMinimumSize(intervalBoxDims);
		updateExprTextbox.setPreferredSize(intervalBoxDims);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		transportArcFeaturesPanel.add(updateExprTextbox, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(transportArcFeaturesPanel, gridBagConstraints);
	}

	private void initColorGuardPanel() {
		colorGuardPanel = new JPanel(new GridBagLayout());
		colorGuardPanel.setBorder(BorderFactory
				.createTitledBorder("Color Guard"));

		colorGuardLabel = new JLabel("Color Guard:");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		colorGuardPanel.add(colorGuardLabel, gridBagConstraints);

		colorGuardTextBox = new JTextField();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		colorGuardPanel.add(colorGuardTextBox, gridBagConstraints);

		colorExampleLabel = new JLabel("Example: 1, 3, 4-6, 8, 12-");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(3, 3, 3, 3);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		colorGuardPanel.add(colorExampleLabel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(0, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(colorGuardPanel, gridBagConstraints);
	}

	private void initButtonPanel(final PetriNetObject objectToBeEdited) {
		buttonPanel = new JPanel(new GridBagLayout());

		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				TimedInputArcComponent arc = (TimedInputArcComponent) objectToBeEdited;
				UndoManager undoManager = CreateGui.getView().getUndoManager();
				undoManager.newEdit();

				dk.aau.cs.model.tapn.TimeInterval guard = composeGuard(arc.getGuard());
				undoManager.addEdit(arc.setGuard(guard));
				CreateGui.getCurrentTab().network().buildConstraints();

				exit();
			}

			private ColoredInterval composeTimeGuard() {
				String openParenthesis = (String) leftDelimiter
						.getSelectedItem();
				String closeParenthesis = (String) rightDelimiter
						.getSelectedItem();

				IntOrConstant lowerScale = new IntOrConstant(lowerScaleTextbox
						.getText());
				IntOrConstant lowerOffset = new IntOrConstant(
						lowerOffsetTextbox.getText());
				IntervalBound a = new IntervalBound(lowerScale, lowerOffset);
				boolean goesToInf = inf.isSelected();

				IntervalBound b = null;
				if (goesToInf) {
					b = new IntervalBound(true);
				} else {
					IntOrConstant upperScale = new IntOrConstant(
							upperScaleTextbox.getText());
					IntOrConstant upperOffset = new IntOrConstant(
							upperOffsetTextbox.getText());

					b = new IntervalBound(upperScale, upperOffset);
				}

				return new ColoredInterval(openParenthesis, a, b,
						closeParenthesis);
			}

			private Preserve getPreservation() {
				if (preserveAgeRadioBtn.isSelected()) {
					return Preserve.Age;
				} else if (preserveValueRadioBtn.isSelected()) {
					return Preserve.Value;
				} else {
					return Preserve.AgeAndValue;
				}
			}

			private ColorSet createColorGuard() {
				ColorSet colorSet = new ColorSet();
				String[] ranges = colorGuardTextBox.getText().split(",");
				for (String range : ranges) {
					try {
						IntOrConstantRange ir = IntOrConstantRange.parse(range
								.trim());
						colorSet.add(ir);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(range, e);
					}
				}
				return colorSet;
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
							(leftDelim == "[" ? true : false), leftInterval,
							rightInterval, (rightDelim == "]" ? true : false));
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
		buttonPanel.add(okButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		buttonPanel.add(cancelButton, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.insets = new Insets(0, 0, 5, 0);
		add(buttonPanel, gridBagConstraints);
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
				if (!CreateGui.getModel().isUsingColors()) {
					if (inf.isSelected()) {
						secondIntervalNumber.setEnabled(false);
						rightDelimiter.setEnabled(false);
					} else {
						secondIntervalNumber.setEnabled(true);
						rightDelimiter.setEnabled(true);
					}
					setDelimiterModels();
				} else {
					if (inf.isSelected()) {
						upperOffsetTextbox.setEnabled(false);
						upperScaleTextbox.setEnabled(false);
						rightDelimiter.setEnabled(false);
						rightDelimiter.setSelectedItem(")");
					} else {
						upperOffsetTextbox.setEnabled(true);
						upperScaleTextbox.setEnabled(true);
						rightDelimiter.setEnabled(true);
						rightDelimiter.setSelectedItem("]");
					}
				}
			}

		});
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 1;
		guardEditPanel.add(inf, gridBagConstraints);

		if (CreateGui.getModel().isUsingColors()) {
			initColoredTimeIntervalControls();
		} else {
			initNonColoredTimeIntervalControls();
		}

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		add(guardEditPanel, gridBagConstraints);
	}

	private void initColoredTimeIntervalControls() {
		JPanel lowerPanel = new JPanel(new GridBagLayout());
		Dimension txtBoxDims = new Dimension(50, 25);

		lowerScaleTextbox = new JTextField();
		lowerScaleTextbox.setMinimumSize(txtBoxDims);
		lowerScaleTextbox.setMaximumSize(txtBoxDims);
		lowerScaleTextbox.setPreferredSize(txtBoxDims);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		lowerPanel.add(lowerScaleTextbox, gbc);

		String mathExprString = "* val +";
		JLabel label = new JLabel(mathExprString);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		lowerPanel.add(label, gbc);

		lowerOffsetTextbox = new JTextField();
		lowerOffsetTextbox.setMinimumSize(txtBoxDims);
		lowerOffsetTextbox.setMaximumSize(txtBoxDims);
		lowerOffsetTextbox.setPreferredSize(txtBoxDims);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		lowerPanel.add(lowerOffsetTextbox, gbc);

		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridx = 3;
		gbc.gridy = 1;
		guardEditPanel.add(new JLabel(" , "), gbc);

		JPanel upperPanel = new JPanel(new GridBagLayout());

		upperScaleTextbox = new JTextField();
		upperScaleTextbox.setMinimumSize(txtBoxDims);
		upperScaleTextbox.setMaximumSize(txtBoxDims);
		upperScaleTextbox.setPreferredSize(txtBoxDims);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		upperPanel.add(upperScaleTextbox, gbc);

		label = new JLabel(mathExprString);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		upperPanel.add(label, gbc);

		upperOffsetTextbox = new JTextField();
		upperOffsetTextbox.setMinimumSize(txtBoxDims);
		upperOffsetTextbox.setMaximumSize(txtBoxDims);
		upperOffsetTextbox.setPreferredSize(txtBoxDims);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		upperPanel.add(upperOffsetTextbox, gbc);

		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(3, 3, 3, 3);
		guardEditPanel.add(upperPanel, gbc);

		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(3, 3, 3, 3);
		guardEditPanel.add(lowerPanel, gbc);
	}

	private void initNonColoredTimeIntervalControls() {
		Dimension intervalBoxDims = new Dimension(90, 25);
		firstIntervalNumber = new JSpinner();
		firstIntervalNumber.setMaximumSize(intervalBoxDims);
		firstIntervalNumber.setMinimumSize(intervalBoxDims);
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
		boolean enableConstantsCheckBoxes = !constants.isEmpty();
		leftUseConstant = new JCheckBox("Use Constant");
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

		leftConstantsComboBox = new JComboBox(constants.toArray());
		leftConstantsComboBox.setVisible(false);
		leftConstantsComboBox.setMaximumSize(intervalBoxDims);
		leftConstantsComboBox.setMinimumSize(intervalBoxDims);
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

		rightUseConstant = new JCheckBox("Use Constant");
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

		rightConstantsComboBox = new JComboBox(constants.toArray());
		rightConstantsComboBox.setVisible(false);
		rightConstantsComboBox.setMaximumSize(intervalBoxDims);
		rightConstantsComboBox.setMinimumSize(intervalBoxDims);
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

		((JDialog) myRootPane.getParent()).pack();
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

	private void updateRightConstantComboBox() {
		int value = getFirstValue();

		String oldRight = rightConstantsComboBox.getSelectedItem() != null ? rightConstantsComboBox
				.getSelectedItem().toString()
				: null;
		rightConstantsComboBox.removeAllItems();
		Collection<Constant> constants = CreateGui.getCurrentTab().network()
				.constants();
		for (Constant c : constants) {
			if (c.value() >= value) {
				rightConstantsComboBox.addItem(c.name());
			}
		}

		// if(rightConstantsComboBox.getItemCount() == 0){
		// rightUseConstant.setEnabled(false);
		// }

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
}