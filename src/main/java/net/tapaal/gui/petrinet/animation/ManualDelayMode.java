package net.tapaal.gui.petrinet.animation;

import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.simulation.DelayMode;
import dk.aau.cs.util.IntervalOperations;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;

/** Swing implementation of the manual delay strategy. */
public class ManualDelayMode implements DelayMode {
    private static ManualDelayMode instance;

    public static ManualDelayMode getInstance() {
        if (instance == null) instance = new ManualDelayMode();
        return instance;
    }

    private ManualDelayMode() {}

    TimeInterval dInterval;
    JButton okButton;
    boolean okPressed = false;
    JDialog dialog;

    @Override
    public String toString() { return name(); }

    public static String name() { return "Manual delay"; }

    @Override
    public BigDecimal GetDelay(TimedTransition transition, TimeInterval dInterval, BigDecimal delayGranularity) {
        this.dInterval = dInterval;
        ChooseDelayPanel panel;
        dialog = new EscapableDialog(TAPAALGUI.getApp(), "Choose delay", true);
        panel = new ChooseDelayPanel(transition, dInterval, delayGranularity);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(TAPAALGUI.getApp());
        dialog.setVisible(true);

        return okPressed ? panel.getResult() : null;
    }

    private class ChooseDelayPanel extends JPanel {
        private final JSpinner spinner;

        ChooseDelayPanel(TimedTransition transition, TimeInterval dInterval, BigDecimal delayGranularity) {
            super(new GridBagLayout());
            JPanel buttonPanel = createButtonPanel();
            BigDecimal value = IntervalOperations.getRatBound(dInterval.lowerBound()).getBound();
            if (!dInterval.isLowerBoundNonStrict()) {
                value = value.add(delayGranularity).stripTrailingZeros();
            }

            SpinnerModel model = new DelaySpinnerModel(value, BigDecimal.ONE, dInterval);
            spinner = new JSpinner(model);
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
            editor.getTextField().addActionListener(arg0 -> {
                okButton.requestFocus();
                okButton.doClick();
            });
            editor.getTextField().setFormatterFactory(new AbstractFormatterFactory() {
                @Override
                public AbstractFormatter getFormatter(JFormattedTextField field) {
                    NumberFormatter formatter = new CustomNumberFormatter();
                    DecimalFormat decimalFormat = new DecimalFormat("#.#####");
                    formatter.setFormat(decimalFormat);
                    formatter.setAllowsInvalid(false);
                    return formatter;
                }
            });
            spinner.setEditor(editor);

            add(new JLabel("Choose delay from the interval: " + dInterval), constraints(0));
            add(spinner, constraints(1));
            add(buttonPanel, constraints(2));
        }

        private GridBagConstraints constraints(int row) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            constraints.insets = new Insets(5, 5, 0, 5);
            return constraints;
        }

        private JPanel createButtonPanel() {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
            okButton = new JButton("OK");
            okButton.setPreferredSize(new java.awt.Dimension(100, 25));
            okButton.setMinimumSize(new java.awt.Dimension(100, 25));
            okButton.setMaximumSize(new java.awt.Dimension(100, 25));
            dialog.getRootPane().setDefaultButton(okButton);
            okButton.addActionListener(arg0 -> {
                okPressed = true;
                dialog.setVisible(false);
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
            cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
            cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
            cancelButton.addActionListener(e -> {
                okPressed = false;
                dialog.setVisible(false);
            });

            buttonPanel.add(cancelButton);
            buttonPanel.add(okButton);
            buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            return buttonPanel;
        }

        private void updateOkButton(BigDecimal result) {
            okButton.setEnabled(result != null && dInterval.isIncluded(result));
        }

        BigDecimal getResult() { return (BigDecimal) spinner.getValue(); }

        private class CustomNumberFormatter extends NumberFormatter {
            @Override
            public Object stringToValue(String string) throws ParseException {
                BigDecimal result;
                try {
                    string = string.replace(DecimalFormatSymbols.getInstance().getDecimalSeparator(), '.');
                    if (string == null || string.isEmpty() || string.equals(".")) {
                        result = null;
                    } else if (string.contains(".") && string.substring(string.indexOf('.') + 1).length() > 5) {
                        throw new ParseException(string, 0);
                    } else {
                        result = new CustomBigDecimal(string);
                    }
                } catch (NumberFormatException e) {
                    throw new ParseException(string, 0);
                }
                updateOkButton(result);
                return result;
            }

            @Override
            public String valueToString(Object value) {
                return value == null ? null : value.toString().replace('.', DecimalFormatSymbols.getInstance().getDecimalSeparator());
            }
        }

        private class CustomBigDecimal extends BigDecimal {
            private final String stringRepresentation;

            CustomBigDecimal(String string) {
                super(string.endsWith(".") && !string.matches(".*\\..*\\..*")
                    ? string.substring(0, string.length() - 1) : string);
                stringRepresentation = string;
            }

            @Override
            public String toString() { return stringRepresentation; }
        }

        private class DelaySpinnerModel extends SpinnerNumberModel {
            private final TimeInterval interval;

            DelaySpinnerModel(Number value, Number stepSize, TimeInterval interval) {
                super(value, null, null, stepSize);
                this.interval = interval;
            }

            @Override
            public Object getNextValue() {
                BigDecimal current = (BigDecimal) getValue();
                return interval.isIncluded(current.add(BigDecimal.ONE)) ? current.add(BigDecimal.ONE) : null;
            }

            @Override
            public Object getPreviousValue() {
                BigDecimal current = (BigDecimal) getValue();
                return interval.isIncluded(current.subtract(BigDecimal.ONE)) ? current.subtract(BigDecimal.ONE) : null;
            }
        }
    }
}
