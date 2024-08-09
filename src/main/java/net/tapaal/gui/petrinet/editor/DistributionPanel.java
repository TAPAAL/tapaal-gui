package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.tapn.*;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.SwingHelper;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.swingcomponents.EscapableDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Arrays;

public class DistributionPanel extends JPanel {

    private TimedTransitionComponent transition;
    private EscapableDialog dialog;

    private static final String[] continuous =  { "constant", "uniform", "exponential", "normal", "gamma" };
    private static final String[] discrete =    { "discrete uniform" };

    public DistributionPanel(TimedTransitionComponent transition, EscapableDialog dialog) {
        this.transition = transition;
        this.dialog = dialog;
        initComponents();
        displayDistribution();
    }

    private void initComponents() {
        useContinuousDistribution = new JRadioButton("Continuous");
        useDiscreteDistribution = new JRadioButton("Discrete");
        distributionCategoryGroup = new ButtonGroup();
        distributionCategoryGroup.add(useContinuousDistribution);
        distributionCategoryGroup.add(useDiscreteDistribution);
        useContinuousDistribution.setSelected(true);

        useContinuousDistribution.addActionListener(act -> {
            distributionType.setModel(new DefaultComboBoxModel<>(continuous));
            displayDistributionFields(SMCDistribution.defaultDistributionFor(String.valueOf(distributionType.getSelectedItem())));

        });
        useDiscreteDistribution.addActionListener(act -> {
            distributionType.setModel(new DefaultComboBoxModel<>(discrete));
            displayDistributionFields(SMCDistribution.defaultDistributionFor(String.valueOf(distributionType.getSelectedItem())));
        });

        distributionType = new JComboBox<>(continuous);
        distributionParam1Label = new JLabel();
        distributionParam2Label = new JLabel();
        distributionParam1Field = new JTextField();
        distributionParam2Field = new JTextField();
        distributionExplanation = new JLabel();
        SwingHelper.setPreferredWidth(distributionParam1Field, 100);
        SwingHelper.setPreferredWidth(distributionParam2Field, 100);
        distributionType.addActionListener(actionEvent -> {
            if(!distributionType.hasFocus()) return;
            displayDistributionFields(SMCDistribution.defaultDistributionFor(String.valueOf(distributionType.getSelectedItem())));
        });
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

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Distribution"));
        GridBagConstraints gbc = GridBagHelper.as(0,0, GridBagHelper.Anchor.WEST, new Insets(3, 3, 3, 3));
        add(useContinuousDistribution, gbc);
        gbc.gridx++;
        add(useDiscreteDistribution, gbc);
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(distributionType, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(distributionParam1Label, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        add(distributionParam1Field, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(distributionParam2Label, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        add(distributionParam2Field, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        add(distributionExplanation, gbc);
    }

    public SMCDistribution parseDistribution() {
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
                case SMCGammaDistribution.NAME:
                    double shape = Double.parseDouble(distributionParam1Field.getText());
                    double scale = Double.parseDouble(distributionParam2Field.getText());
                    return new SMCGammaDistribution(shape, scale);
                case SMCDiscreteUniformDistribution.NAME:
                    double da = Integer.parseInt(distributionParam1Field.getText());
                    double db = Integer.parseInt(distributionParam2Field.getText());
                    return new SMCDiscreteUniformDistribution(da, db);
            }
        } catch(NumberFormatException ignored) {}
        return SMCDistribution.defaultDistributionFor(type);
    }

    public void displayDistribution() {
        SMCDistribution distribution = transition.underlyingTransition().getDistribution();
        displayDistributionFields(distribution);
    }

    public void displayDistributionFields(SMCDistribution distribution) {
        if(Arrays.asList(continuous).contains(distribution.distributionName())) {
            useContinuousDistribution.setSelected(true);
        } else {
            useDiscreteDistribution.setSelected(true);
        }
        switch (distribution.distributionName()) {
            case SMCConstantDistribution.NAME:
                displayOneVariable("Value", ((SMCConstantDistribution) distribution).value);
                break;
            case SMCUniformDistribution.NAME:
                displayTwoVariables(
                    "A", ((SMCUniformDistribution) distribution).a,
                    "B", ((SMCUniformDistribution) distribution).b);
                break;
            case SMCExponentialDistribution.NAME:
                displayOneVariable("Rate", ((SMCExponentialDistribution) distribution).rate);
                break;
            case SMCNormalDistribution.NAME:
                displayTwoVariables(
                    "Mean", ((SMCNormalDistribution) distribution).mean,
                    "Std. Dev", ((SMCNormalDistribution) distribution).stddev);
                break;
            case SMCGammaDistribution.NAME:
                displayTwoVariables(
                    "Shape", ((SMCGammaDistribution) distribution).shape,
                    "Scale", ((SMCGammaDistribution) distribution).scale);
                break;
            case SMCDiscreteUniformDistribution.NAME:
                displayTwoVariables(
                    "A", ((SMCDiscreteUniformDistribution) distribution).a,
                    "B", ((SMCDiscreteUniformDistribution) distribution).b);
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

    private void displayOneVariable(String name, double value) {
        distributionParam1Label.setText(name + " :");
        distributionParam1Field.setText(String.valueOf(value));
        distributionParam2Label.setVisible(false);
        distributionParam2Field.setVisible(false);
    }

    private void displayTwoVariables(String name1, double value1, String name2, double value2) {
        distributionParam1Label.setText(name1 + " :");
        distributionParam2Label.setText(name2 + " :");
        distributionParam1Field.setText(String.valueOf(value1));
        distributionParam2Field.setText(String.valueOf(value2));
        distributionParam2Label.setVisible(true);
        distributionParam2Field.setVisible(true);
    }

    private JRadioButton useContinuousDistribution;
    private JRadioButton useDiscreteDistribution;
    private ButtonGroup distributionCategoryGroup;
    private JComboBox<String> distributionType;
    private JLabel distributionParam1Label;
    private JLabel distributionParam2Label;
    private JTextField distributionParam1Field;
    private JTextField distributionParam2Field;
    private JLabel distributionExplanation;

}
