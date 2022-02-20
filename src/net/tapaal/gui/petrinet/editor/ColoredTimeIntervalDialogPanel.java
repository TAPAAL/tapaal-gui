package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.tapn.Bound;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.IntBound;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.WidthAdjustingComboBox;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;

public class ColoredTimeIntervalDialogPanel extends JPanel {

    ColoredTimeInterval coloredTimeInterval;

    JPanel guardEditPanel;
    JLabel label;
    JComboBox<String> leftDelimiter;
    JComboBox<String> rightDelimiter;
    JCheckBox inf;
    JSpinner secondIntervalNumber;
    JSpinner firstIntervalNumber;
    JCheckBox leftUseConstant;
    JComboBox<String> leftConstantsComboBox;
    JComboBox<String> rightConstantsComboBox;
    JCheckBox rightUseConstant;
    final int maxNumberOfPlacesToShowAtOnce = 20;

    public ColoredTimeIntervalDialogPanel(ColoredTimeInterval cti) {
        this.coloredTimeInterval = cti;
        initPanel();

        setTimeInterval(cti);
    }
    public ColoredTimeInterval getInterval() {
        return composeGuard(coloredTimeInterval);
    }

    public void setTimeInterval(ColoredTimeInterval cti) {
        String intervalAsString = cti.getInterval();
        coloredTimeInterval = cti;

        String[] partedTimeInterval = intervalAsString.split(",");
        String firstNumber = partedTimeInterval[0].substring(1);
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
        SpinnerNumberModel spinnerModelForFirstNumber = new SpinnerNumberModel(first, 0, Integer.MAX_VALUE, 1);

        SpinnerNumberModel spinnerModelForSecondNumber;
        boolean isInf = secondNumber.equals("inf");
        if (isInf) {
            inf.setSelected(true);
            secondIntervalNumber.setEnabled(false);
            rightDelimiter.setEnabled(false);
            spinnerModelForSecondNumber = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        } else {
            inf.setSelected(false);
            secondIntervalNumber.setEnabled(true);
            rightDelimiter.setEnabled(true);
            spinnerModelForSecondNumber = new SpinnerNumberModel(second, 0, Integer.MAX_VALUE, 1);
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
        if (intervalAsString.contains("[")) {
            leftDelimiter.setSelectedItem("[");
        } else {
            leftDelimiter.setSelectedItem("(");
        }
        if (intervalAsString.contains("]")) {
            rightDelimiter.setSelectedItem("]");
        } else {
            rightDelimiter.setSelectedItem(")");
        }
    }


    private ColoredTimeInterval composeGuard(ColoredTimeInterval oldGuard) {
        boolean useConstantLeft = leftUseConstant.isSelected();
        boolean useConstantRight = rightUseConstant.isSelected();

        String leftDelim = Objects.requireNonNull(leftDelimiter.getSelectedItem()).toString();
        String rightDelim = Objects.requireNonNull(rightDelimiter.getSelectedItem()).toString();
        Bound leftInterval;
        Bound rightInterval;

        if (useConstantLeft) {
            String constantName = Objects.requireNonNull(leftConstantsComboBox.getSelectedItem()).toString();
            leftInterval = new ConstantBound(TAPAALGUI.getCurrentTab().network().getConstant(constantName));
        } else {
            leftInterval = new IntBound((Integer) firstIntervalNumber.getValue());
        }

        if (useConstantRight) {
            String constantName = Objects.requireNonNull(rightConstantsComboBox.getSelectedItem()).toString();
            rightInterval = new ConstantBound(TAPAALGUI.getCurrentTab().network().getConstant(constantName));
        } else if (inf.isSelected()) {
            rightInterval = Bound.Infinity;
        } else {
            rightInterval = new IntBound((Integer) secondIntervalNumber.getValue());
        }

        if (rightInterval instanceof Bound.InfBound || leftInterval.value() <= rightInterval.value()) {
            return new ColoredTimeInterval(
                    (leftDelim.equals("[")), leftInterval,
                    rightInterval, (rightDelim.equals("]")), coloredTimeInterval.getColor()
                );
        } else {
            return oldGuard;
        }
    }

    private void initPanel() {
        guardEditPanel = new JPanel(new GridBagLayout());
        guardEditPanel.setBorder(BorderFactory.createTitledBorder("Time Guard"));

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

        inf = new JCheckBox("inf", true);
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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 10, 5, 10);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(guardEditPanel, gridBagConstraints);
    }

    private void initNonColoredTimeIntervalControls() {
        Dimension intervalBoxDims = new Dimension(190, 25);

        firstIntervalNumber = new JSpinner();

        firstIntervalNumber.setPreferredSize(intervalBoxDims);
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
        secondIntervalNumber.setMaximumSize(intervalBoxDims);
        secondIntervalNumber.setMinimumSize(intervalBoxDims);
        secondIntervalNumber.setPreferredSize(intervalBoxDims);
        secondIntervalNumber.addChangeListener(this::secondSpinnerStateChanged);

        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        guardEditPanel.add(secondIntervalNumber, gridBagConstraints);

        Set<String> constants = TAPAALGUI.getCurrentTab().network().getConstantNames();
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

        leftConstantsComboBox.setPreferredSize(intervalBoxDims);
        leftConstantsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateRightConstantComboBox();
                setDelimiterModels();
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
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
        gridBagConstraints.weightx = 0.0;

        guardEditPanel.add(rightUseConstant, gridBagConstraints);

        rightConstantsComboBox = new WidthAdjustingComboBox<>(maxNumberOfPlacesToShowAtOnce);
        rightConstantsComboBox.setModel(new DefaultComboBoxModel<>(constantArray));
        rightConstantsComboBox.setMaximumRowCount(20);
        rightConstantsComboBox.setVisible(false);

        rightConstantsComboBox.setPreferredSize(intervalBoxDims);
        gridBagConstraints = new GridBagConstraints();
        rightConstantsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setDelimiterModels();
            }
        });

        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;

        guardEditPanel.add(rightConstantsComboBox, gridBagConstraints);
    }

    private void setDelimiterModels() {
        int firstValue = getFirstValue();
        int secondValue = getSecondValue();

        DefaultComboBoxModel<String> modelRightIncludedOnly = new DefaultComboBoxModel<>(
                new String[] { "]" });
        DefaultComboBoxModel<String> modelLeftIncludedOnly = new DefaultComboBoxModel<>(
                new String[] { "[" });
        DefaultComboBoxModel<String> modelRightBoth = new DefaultComboBoxModel<>(
                new String[] { "]", ")" });
        DefaultComboBoxModel<String> modelLeftBoth = new DefaultComboBoxModel<>(
                new String[] { "[", "(" });
        DefaultComboBoxModel<String> modelRightExcludedOnly = new DefaultComboBoxModel<>(
                new String[] { ")" });

        if (firstValue > secondValue) {
            secondIntervalNumber.setValue(firstValue);
            secondValue = firstValue;
        }

        String leftOldDelim = Objects.requireNonNull(leftDelimiter.getSelectedItem()).toString();
        String rightOldDelim = Objects.requireNonNull(rightDelimiter.getSelectedItem()).toString();

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

    private void secondSpinnerStateChanged(ChangeEvent evt) {
        setDelimiterModels();
    }

    private void firstSpinnerStateChanged(ChangeEvent evt) {
        int firstValue = getFirstValue();
        int secondValue = getSecondValue();
        if (rightUseConstant.isSelected() && firstValue > secondValue) {
            rightUseConstant.setSelected(false);
            updateRightComponents();
        }
        if (firstValue > TAPAALGUI.getCurrentTab().network()
                .getLargestConstantValue())
            rightUseConstant.setEnabled(false);
        else {
            rightUseConstant.setEnabled(true);
            updateRightConstantComboBox();
        }
        setDelimiterModels();
    }

    private void updateLeftComponents() {
        boolean value = leftUseConstant.isSelected();
        firstIntervalNumber.setVisible(!value);
        leftConstantsComboBox.setVisible(value);
        setDelimiterModels();
    }

    private int getSecondValue() {
        int secondValue;
        if (rightUseConstant.isSelected()) {
            secondValue = TAPAALGUI.getCurrentTab().network().getConstantValue(
                    Objects.requireNonNull(rightConstantsComboBox.getSelectedItem()).toString());
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
            firstValue = TAPAALGUI.getCurrentTab().network().getConstantValue(
                    Objects.requireNonNull(leftConstantsComboBox.getSelectedItem()).toString());
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
        Collection<Constant> constants = TAPAALGUI.getCurrentTab().network()
                .constants();

        //List <Constant> constantList = new ArrayList(constants);
        List<Constant> constantList = new ArrayList<>(constants);

        constantList.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));


        for (Constant c : constantList) {
            if (c.value() >= value) {
                rightConstantsComboBox.addItem(c.name());
            }
        }

        rightUseConstant.setEnabled(rightConstantsComboBox.getItemCount() != 0);

        if (oldRight != null)
            rightConstantsComboBox.setSelectedItem(oldRight);
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

        setDelimiterModels();
    }

}
