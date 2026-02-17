package net.tapaal.gui.petrinet.editor;

import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.Require;
import dk.aau.cs.util.RequireException;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.SwingHelper;
import pipe.gui.TAPAALGUI;
import pipe.gui.graph.Graph;
import pipe.gui.graph.GraphDialog;
import pipe.gui.graph.GraphPoint;
import pipe.gui.graph.DefaultGraphDialog.GraphDialogBuilder;
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.swingcomponents.EscapableDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public class DistributionPanel extends JPanel {

    private TimedTransitionComponent transition;
    private EscapableDialog dialog;
    private boolean updatingFields;

    private static final String[] continuous =  { "constant", "uniform", "exponential", "normal", "gamma", "erlang", "triangular", "log normal" };
    private static final String[] discrete =    { "discrete uniform", "geometric" };

    private JRadioButton useContinuousDistribution;
    private JRadioButton useDiscreteDistribution;
    private JRadioButton useCustomDistribution;
    private ButtonGroup distributionCategoryGroup;
    private JComboBox<String> distributionType;
    private JButton distributionShowGraph;
    private JButton manageCustomDistributionsButton;
    private JButton okButton;
    private JLabel distributionParam1Label;
    private JLabel distributionParam2Label;
    private JLabel distributionParam3Label;
    private JTextField distributionParam1Field;
    private JTextField distributionParam2Field;
    private JTextField distributionParam3Field;
    private JLabel meanLabel;
    private JLabel meanValueLabel;

    public DistributionPanel(TimedTransitionComponent transition, JButton okButton, EscapableDialog dialog) {
        this.transition = transition;
        this.okButton = okButton;
        this.dialog = dialog;
        initComponents();
        displayDistribution();
    }

    private void initComponents() {
        useContinuousDistribution = new JRadioButton("Continuous");
        useDiscreteDistribution = new JRadioButton("Discrete");
        useCustomDistribution = new JRadioButton("Custom");
        distributionCategoryGroup = new ButtonGroup();
        distributionCategoryGroup.add(useContinuousDistribution);
        distributionCategoryGroup.add(useDiscreteDistribution);
        distributionCategoryGroup.add(useCustomDistribution);
        useContinuousDistribution.setSelected(true);

        useContinuousDistribution.addActionListener(act -> updateDistributionCategory());
        useDiscreteDistribution.addActionListener(act -> updateDistributionCategory());
        useCustomDistribution.addActionListener(act -> updateDistributionCategory());

        distributionType = new JComboBox<>(continuous);
        distributionType.setPreferredSize(new Dimension(150, distributionType.getPreferredSize().height));
        manageCustomDistributionsButton = new JButton("Manage distributions");
        manageCustomDistributionsButton.setVisible(false);
        manageCustomDistributionsButton.addActionListener(e -> showManageCustomDistributionsDialog());

        distributionShowGraph = new JButton("Show density");
        distributionParam1Label = new JLabel();
        distributionParam2Label = new JLabel();
        distributionParam3Label = new JLabel();
        distributionParam1Field = new JTextField(10);
        distributionParam2Field = new JTextField(10);
        distributionParam3Field = new JTextField(10);

        meanLabel = new JLabel();
        meanValueLabel = new JLabel();
        SwingHelper.setPreferredWidth(distributionParam1Field, 150);
        SwingHelper.setPreferredWidth(distributionParam2Field, 150);
        SwingHelper.setPreferredWidth(distributionParam3Field, 150);
        distributionType.addActionListener(actionEvent -> {
            if(!distributionType.hasFocus()) return;
            if (useCustomDistribution.isSelected()) {
                 String selectedName = (String)distributionType.getSelectedItem();
                 if (selectedName != null) {
                     displayDistributionFields(new SMCUserDefinedDistribution(selectedName));
                 }
            } else {
                displayDistributionFields(SMCDistribution.defaultDistributionFor(String.valueOf(distributionType.getSelectedItem())));
            }
        });
        distributionShowGraph.addActionListener(actionEvent -> showDistributionGraph());
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
                if (distrib.getMean() != null && !(distrib instanceof SMCNormalDistribution) && !(distrib instanceof SMCExponentialDistribution)) {
                    meanLabel.setText("Mean :");
                    meanValueLabel.setText(formatValue(distrib.getMean()));
                } else {
                    meanLabel.setText("");
                    meanValueLabel.setText("");
                }
                distributionType.setToolTipText(distrib.explanation());
            }
        };

        final DocumentListener rateFieldListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::updateMeanFromRate); }
            @Override
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::updateMeanFromRate); }
            @Override
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::updateMeanFromRate); }
            
            private void updateMeanFromRate() {
                if (distributionType.getSelectedItem() != null && distributionType.getSelectedItem().equals(SMCExponentialDistribution.NAME) && 
                    !updatingFields && distributionParam1Field.hasFocus()) {
                    try {
                        updatingFields = true;
                        String text = distributionParam1Field.getText();
                        if (!text.isEmpty()) {
                            double rate = Double.parseDouble(text);
                            distributionParam2Field.setText(formatValue(1.0 / rate));
                            distributionParam2Field.setCaretPosition(0);
                        }
                    } catch (NumberFormatException ignored) {
                    } finally {
                        updatingFields = false;
                    }
                }
            }
        };
        
        final DocumentListener meanFieldListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::updateRateFromMean); }
            @Override
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::updateRateFromMean); }
            @Override
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::updateRateFromMean); }
            
            private void updateRateFromMean() {
                if (distributionType.getSelectedItem() != null && distributionType.getSelectedItem().equals(SMCExponentialDistribution.NAME) && 
                    !updatingFields && distributionParam2Field.hasFocus()) {
                    try {
                        updatingFields = true;
                        String text = distributionParam2Field.getText();
                        if (!text.isEmpty()) {
                            double mean = Double.parseDouble(text);
                            distributionParam1Field.setText(formatValue(1.0 / mean));
                            distributionParam1Field.setCaretPosition(0);
                        }
                    } catch (NumberFormatException ignored) {
                    } finally {
                        updatingFields = false;
                    }
                }
            }
        };

        distributionParam1Field.getDocument().addDocumentListener(rateFieldListener);
        distributionParam2Field.getDocument().addDocumentListener(meanFieldListener);

        distributionParam1Field.getDocument().addDocumentListener(updateDistribDisplay);
        distributionParam2Field.getDocument().addDocumentListener(updateDistribDisplay);
        distributionParam3Field.getDocument().addDocumentListener(updateDistribDisplay);

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Distribution and Firing Mode"));
        GridBagConstraints gbc = GridBagHelper.as(0,0, GridBagHelper.Anchor.WEST, new Insets(3, 3, 3, 3));
        add(useContinuousDistribution, gbc);
        gbc.gridx++;
        add(useDiscreteDistribution, gbc);
        gbc.gridx++;
        add(useCustomDistribution, gbc);
        gbc.gridx++;
        add(distributionType, gbc);
        
        gbc.gridx++;
        add(manageCustomDistributionsButton, gbc);
        
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        add(distributionShowGraph, gbc);

        JPanel paramPanel = new JPanel(new GridBagLayout());
        gbc = GridBagHelper.as(0,0, GridBagHelper.Anchor.WEST, new Insets(3, 3, 3, 3));
        paramPanel.add(distributionParam1Label, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paramPanel.add(distributionParam1Field, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        paramPanel.add(distributionParam2Label, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paramPanel.add(distributionParam2Field, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx++;
        gbc.gridwidth = 1;
        paramPanel.add(meanLabel, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        paramPanel.add(meanValueLabel, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx -= 5;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        paramPanel.add(distributionParam3Label, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        paramPanel.add(distributionParam3Field, gbc);
        gbc = GridBagHelper.as(0, 1, GridBagHelper.Anchor.WEST, new Insets(3, 3, 3, 3));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 6;
        paramPanel.setPreferredSize(new Dimension(765, paramPanel.getPreferredSize().height));
        add(paramPanel, gbc);

        setUrgent(transition.underlyingTransition().isUrgent());
    }

    private void updateDistributionCategory() {
        updateDistributionCategory(null);
    }

    private void updateDistributionCategory(String selectedDistribution) {
        if (useCustomDistribution.isSelected()) {
            TimedArcPetriNetNetwork network = transition.underlyingTransition().model().parentNetwork();
            List<String> names = network.userDefinedDistributions().stream()
                .map(SMCUserDefinedDistribution::getName)
                .collect(Collectors.toList());
            distributionType.setModel(new DefaultComboBoxModel<>(names.toArray(new String[0])));
            manageCustomDistributionsButton.setVisible(true);
            
            if (!names.isEmpty()) {
                if (selectedDistribution != null && names.contains(selectedDistribution)) {
                    distributionType.setSelectedItem(selectedDistribution);
                    displayDistributionFields(new SMCUserDefinedDistribution(selectedDistribution));
                } else {
                    distributionType.setSelectedIndex(0);
                    displayDistributionFields(new SMCUserDefinedDistribution(names.get(0)));
                }
                
                distributionShowGraph.setEnabled(true);
                okButton.setEnabled(true);
            } else {
                distributionType.setSelectedItem(null);
                displayCustomDistribution();
                meanLabel.setText("");
                meanValueLabel.setText("");
                distributionShowGraph.setEnabled(false);
                okButton.setEnabled(false);
            }
        } else {
            distributionShowGraph.setEnabled(true);
            okButton.setEnabled(true);
            manageCustomDistributionsButton.setVisible(false);
            boolean toContinuous = useContinuousDistribution.isSelected();
            String currentDistribution = String.valueOf(distributionType.getSelectedItem());
            String a = distributionParam1Field.getText();
            String b = distributionParam2Field.getText();
            
            distributionType.setModel(new DefaultComboBoxModel<>(toContinuous ? continuous : discrete));

            boolean isUniformConversion = (toContinuous && SMCDiscreteUniformDistribution.NAME.equals(currentDistribution)) ||
                                        (!toContinuous && SMCUniformDistribution.NAME.equals(currentDistribution));
            
            if (isUniformConversion) {
                String targetDistribution = toContinuous ? SMCUniformDistribution.NAME : SMCDiscreteUniformDistribution.NAME;
                distributionType.setSelectedItem(targetDistribution);
                displayDistributionFields(SMCDistribution.defaultDistributionFor(targetDistribution));
                distributionParam1Field.setText(a);
                distributionParam2Field.setText(b);
            } else {
                displayDistributionFields(SMCDistribution.defaultDistributionFor(String.valueOf(distributionType.getSelectedItem())));
            }
        }

        dialog.pack();
    }

    public void setUrgent(boolean urgent) {
        if(urgent) {
            displayDistributionFields(SMCDistribution.urgent());
            distributionType.setEnabled(false);
            useDiscreteDistribution.setEnabled(false);
            useContinuousDistribution.setEnabled(false);
            useCustomDistribution.setEnabled(false);
            distributionParam1Field.setEnabled(false);
        } else {
            distributionType.setEnabled(true);
            useDiscreteDistribution.setEnabled(true);
            useContinuousDistribution.setEnabled(true);
            useCustomDistribution.setEnabled(true);
            distributionParam1Field.setEnabled(true);
        }
    }

    public SMCDistribution parseDistribution() {
        if(transition.isUrgent()) {
            return SMCDistribution.urgent();
        }
        String type = String.valueOf(distributionType.getSelectedItem());
        if (useCustomDistribution.isSelected()) {
             return new SMCUserDefinedDistribution(type);
        }
        
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
                case SMCErlangDistribution.NAME:
                    double eshape = Integer.parseInt(distributionParam1Field.getText());
                    double escale = Double.parseDouble(distributionParam2Field.getText());
                    return new SMCErlangDistribution(eshape, escale);
                case SMCDiscreteUniformDistribution.NAME:
                    double da = Integer.parseInt(distributionParam1Field.getText());
                    double db = Integer.parseInt(distributionParam2Field.getText());
                    return new SMCDiscreteUniformDistribution(da, db);
                case SMCGeometricDistribution.NAME:
                    double p = Double.parseDouble(distributionParam1Field.getText());
                    return new SMCGeometricDistribution(p);
                case SMCTriangularDistribution.NAME:
                    double ta = Double.parseDouble(distributionParam1Field.getText());
                    double tb = Double.parseDouble(distributionParam2Field.getText());
                    double tc = Double.parseDouble(distributionParam3Field.getText());
                    return new SMCTriangularDistribution(ta, tb, tc);
                case SMCLogNormalDistribution.NAME:
                    double logMean = Double.parseDouble(distributionParam1Field.getText());
                    double logStddev = Double.parseDouble(distributionParam2Field.getText());
                    return new SMCLogNormalDistribution(logMean, logStddev);
            }
        } catch(NumberFormatException ignored) {}
        return SMCDistribution.defaultDistributionFor(type);
    }

    public void displayDistribution() {
        SMCDistribution distribution = transition.underlyingTransition().getDistribution();
        displayDistributionFields(distribution);
    }

    public void displayDistributionFields(SMCDistribution distribution) {
        if (Arrays.asList(continuous).contains(distribution.distributionName())) {
            useContinuousDistribution.setSelected(true);
            distributionType.setModel(new DefaultComboBoxModel<>(continuous));
            manageCustomDistributionsButton.setVisible(false);
            distributionShowGraph.setEnabled(true);
            okButton.setEnabled(true);
        } else if (Arrays.asList(discrete).contains(distribution.distributionName())) {
            distributionType.setModel(new DefaultComboBoxModel<>(discrete));
            useDiscreteDistribution.setSelected(true);
            manageCustomDistributionsButton.setVisible(false);
            distributionShowGraph.setEnabled(true);
            okButton.setEnabled(true);
        } else if (distribution instanceof SMCUserDefinedDistribution) {
            useCustomDistribution.setSelected(true);
            manageCustomDistributionsButton.setVisible(true);
            TimedArcPetriNetNetwork network = transition.underlyingTransition().model().parentNetwork();
            List<String> names = network.userDefinedDistributions().stream()
                                        .map(SMCUserDefinedDistribution::getName)
                                        .collect(Collectors.toList());
            distributionType.setModel(new DefaultComboBoxModel<>(names.toArray(new String[0])));
            distributionShowGraph.setEnabled(!names.isEmpty());
            okButton.setEnabled(!names.isEmpty());
        }
        
        distributionParam1Field.setEditable(true);

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
                displayTwoVariables("Rate", ((SMCExponentialDistribution) distribution).rate,
                                  "Mean", ((SMCExponentialDistribution) distribution).getMean());
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
            case SMCErlangDistribution.NAME:
                displayTwoVariables(
                    "Shape", ((SMCErlangDistribution) distribution).shape,
                    "Scale", ((SMCErlangDistribution) distribution).scale);
                break;
            case SMCDiscreteUniformDistribution.NAME:
                displayTwoVariables(
                    "A", ((SMCDiscreteUniformDistribution) distribution).a,
                    "B", ((SMCDiscreteUniformDistribution) distribution).b);
                break;
            case SMCGeometricDistribution.NAME:
                displayOneVariable("P", ((SMCGeometricDistribution) distribution).p);
                break;
            case SMCTriangularDistribution.NAME:
                displayThreeVariables(
                    "A", ((SMCTriangularDistribution) distribution).a,
                    "B", ((SMCTriangularDistribution) distribution).b,
                    "C", ((SMCTriangularDistribution) distribution).c);
                break;
            case SMCLogNormalDistribution.NAME:
                displayTwoVariables(
                    "Log Mean", ((SMCLogNormalDistribution) distribution).logMean,
                    "Log Std. Dev", ((SMCLogNormalDistribution) distribution).logStddev);
                break;
            case SMCUserDefinedDistribution.NAME:
                displayCustomDistribution();
                break;
            default:
                break;
        }
        distributionType.setToolTipText(distribution.explanation());

        if (distribution.getMean() != null && !(distribution instanceof SMCNormalDistribution) && !(distribution instanceof SMCExponentialDistribution)) {
            meanLabel.setText("Mean :");
            meanValueLabel.setText(String.format("%.3f", distribution.getMean()));
        } else {
            meanLabel.setText("");
            meanValueLabel.setText("");
        }

        distributionType.setFocusable(false);
        if (distribution instanceof SMCUserDefinedDistribution) {
             distributionType.setSelectedItem(((SMCUserDefinedDistribution)distribution).getName());
        } else {
             distributionType.setSelectedItem(distribution.distributionName());
        }
        distributionType.setFocusable(true);
        dialog.pack();
    }

    private void displayOneVariable(String name, double value) {
        distributionParam1Label.setText(name + " :");
        distributionParam1Field.setText(formatValue(value));
        distributionParam1Field.setCaretPosition(0);
        distributionParam2Label.setVisible(false);
        distributionParam2Field.setVisible(false);
        distributionParam3Label.setVisible(false);
        distributionParam3Field.setVisible(false);
        distributionParam1Field.setVisible(true);
    }

    private void displayTwoVariables(String name1, double value1, String name2, double value2) {
        distributionParam1Label.setText(name1 + " :");
        distributionParam2Label.setText(name2 + " :");
        distributionParam1Field.setText(formatValue(value1));
        distributionParam2Field.setText(formatValue(value2));
        distributionParam1Field.setCaretPosition(0);
        distributionParam2Field.setCaretPosition(0);
        distributionParam2Label.setVisible(true);
        distributionParam2Field.setVisible(true);
        distributionParam3Label.setVisible(false);
        distributionParam3Field.setVisible(false);
        distributionParam1Field.setVisible(true);
    }

    private void displayThreeVariables(String name1, double value1, String name2, double value2, String name3, double value3) {
        distributionParam1Label.setText(name1 + " :");
        distributionParam2Label.setText(name2 + " :");
        distributionParam3Label.setText(name3 + " :");
        distributionParam1Field.setText(formatValue(value1));
        distributionParam2Field.setText(formatValue(value2));
        distributionParam3Field.setText(formatValue(value3));
        distributionParam1Field.setCaretPosition(0);
        distributionParam2Field.setCaretPosition(0);
        distributionParam3Field.setCaretPosition(0);
        distributionParam2Label.setVisible(true);
        distributionParam2Field.setVisible(true);
        distributionParam3Label.setVisible(true);
        distributionParam3Field.setVisible(true);
        distributionParam1Field.setVisible(true);
    }

    private void displayCustomDistribution() {
        distributionParam1Label.setText("");
        distributionParam1Field.setVisible(false);
        distributionParam2Label.setVisible(false);
        distributionParam2Field.setVisible(false);
        distributionParam3Label.setVisible(false);
        distributionParam3Field.setVisible(false);
    }

    private void showManageCustomDistributionsDialog() {
        String previouslySelected = (String)distributionType.getSelectedItem();
        TimedArcPetriNetNetwork network = transition.underlyingTransition().model().parentNetwork();

        SMCUserDefinedDistribution previousDist = null;
        if (previouslySelected != null) {
            for (SMCUserDefinedDistribution dist : network.userDefinedDistributions()) {
                if (dist.getName().equals(previouslySelected)) {
                    previousDist = dist;
                    break;
                }
            }
        }

        ManageCustomDistributionsDialog dialog = new ManageCustomDistributionsDialog(network, this);
        dialog.setVisible(true);
        if (useCustomDistribution.isSelected()) {
             if (previousDist != null && network.userDefinedDistributions().contains(previousDist)) {
                 updateDistributionCategory(previousDist.getName());
             } else {
                 updateDistributionCategory(previouslySelected);
             }
        }
    }

    private String formatValue(double value) {
        DecimalFormat df = new DecimalFormat("#.################", new DecimalFormatSymbols(Locale.ENGLISH));
        return df.format(value);
    }

    private void showDistributionGraph() {
        SMCDistribution distribution = parseDistribution();

        try {
            GraphDialog dialog = createGraphDialog(distribution);
            dialog.display();
        } catch (RequireException e) {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "There was an error opening the graph. Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private GraphDialog createGraphDialog(SMCDistribution distribution) {
        String title = "Probability Density Function";
        GraphDialogBuilder builder = new GraphDialogBuilder();

        if (distribution instanceof SMCConstantDistribution) {
            Graph graph = createGraph((SMCConstantDistribution) distribution);
            builder = builder.addGraph(graph).setTitle(title);
        } else if (distribution instanceof SMCDiscreteUniformDistribution) {
            Graph graph = createGraph((SMCDiscreteUniformDistribution) distribution);
            builder = builder.addGraph(graph).setPointPlot(true);
        } else if (distribution instanceof SMCExponentialDistribution) {
            Graph graph = createGraph((SMCExponentialDistribution) distribution);
            builder = builder.addGraph(graph);
        } else if (distribution instanceof SMCGammaDistribution) {
            Graph graph = createGraph((SMCGammaDistribution) distribution);
            builder = builder.addGraph(graph);
        } else if (distribution instanceof SMCErlangDistribution) {
            Graph graph = createGraph((SMCErlangDistribution) distribution);
            builder = builder.addGraph(graph);
        } else if (distribution instanceof SMCNormalDistribution) {
            Graph graph = createGraph((SMCNormalDistribution) distribution);
            builder = builder.addGraph(graph).setTitle(title);
        } else if (distribution instanceof SMCUniformDistribution) {
            List<Graph> graphs = createGraphs((SMCUniformDistribution) distribution);
            builder = builder.addGraphs(graphs).setPiecewise(true);
        } else if (distribution instanceof SMCGeometricDistribution) {
            Graph graph = createGraph((SMCGeometricDistribution) distribution);
            builder = builder.addGraph(graph).setPointPlot(true);
        } else if (distribution instanceof SMCTriangularDistribution) {
            Graph graph = createGraph((SMCTriangularDistribution) distribution);
            builder = builder.addGraph(graph);
        } else if (distribution instanceof SMCLogNormalDistribution) {
            Graph graph = createGraph((SMCLogNormalDistribution) distribution);
            builder = builder.addGraph(graph);
        } else if (distribution instanceof SMCUserDefinedDistribution) {
            Graph graph = createGraph((SMCUserDefinedDistribution) distribution);
            builder = builder.addGraph(graph).setPointPlot(true);
        }

        return builder.setTitle(title).build();
    }

    private Graph createGraph(SMCConstantDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();
        LinkedHashMap<String, Double> params = distribution.getParameters();
        double value = params.get("value");

        points.add(new GraphPoint(value, 0));
        points.add(new GraphPoint(value, 100));

        return new Graph("Constant Distribution", points, distribution.getMean());
    }

    private Graph createGraph(SMCDiscreteUniformDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double a = params.get("a");
        double b = params.get("b");
        double mean = distribution.getMean();

        double n = b - a + 1;

        for (int x = (int) a; x <= (int) b; ++x) {
            points.add(new GraphPoint(x, 1 / n));
        }

        return new Graph("Discrete Uniform Distribution", points, mean);
    }

    private Graph createGraph(SMCExponentialDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double rate = params.get("rate");
        double mean = distribution.getMean();

        int x = 0;
        while (true) {
            double y = rate * Math.exp(-rate * x);
            if (y < 1e-6) break;
            points.add(new GraphPoint(x, y));
            ++x;
        }

        return new Graph("Exponential Distribution", points, mean);
    }

    private Graph createGraph(SMCGammaDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double shape = params.get("shape");
        double scale = params.get("scale");

        Require.that(shape > 0, "Shape must be a positive real");
        Require.that(scale > 0, "Scale must be a positive real");

        double gamma = spougeGammaApprox(shape - 1);
        double coefficient = 1 / (gamma * Math.pow(scale, shape));
        double step = 0.1;

        // Start at some arbitrary small value to avoid division by zero
        double x = 1e-300;
        while (true) {
            double y = coefficient * Math.pow(x, shape - 1) * Math.exp(-(x / scale));
            if (y < 1e-8 && x > 10) break;
            points.add(new GraphPoint(x, y));
            x += step;	
        }

        return new Graph("Gamma Distribution", points, distribution.getMean());
    }

    private Graph createGraph(SMCErlangDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double shape = params.get("shape");
        double scale = params.get("scale");

        Require.that(shape >= 1 && (shape % 1 == 0), "Shape must be a positive integer");
        Require.that(scale > 0, "Scale must be a positive real");

        double gamma = spougeGammaApprox(shape - 1);
        double coefficient = 1 / (gamma * Math.pow(scale, shape));
        double step = 0.1;

        // Start at some arbitrary small value to avoid division by zero
        double x = 1e-300;
        while (true) {
            double y = coefficient * Math.pow(x, shape - 1) * Math.exp(-(x / scale));
            if (y < 1e-8 && x > 10) break;
            points.add(new GraphPoint(x, y));
            x += step;
        }

        return new Graph("Erlang Distribution", points, distribution.getMean());
    }

    private double spougeGammaApprox(double shape) {
        int a = 10;
        List<Double> c = new ArrayList<>();
        c.add(Math.sqrt(2 * Math.PI));
        for (int k = 1; k < a; ++k) {
            c.add((Math.pow(-1, k - 1) / factorial(k - 1)) * Math.pow(-k + a, k - 0.5) * Math.exp(-k + a));
        }

        double sum = c.get(0);
        for (int k = 1; k < a; ++k) {
            sum += c.get(k) / (shape + k);
        }

        double term = Math.pow(shape + a, shape + 0.5) * Math.exp(-shape - a);
        return term * sum;
    }

    private int factorial(int n) {
        int result = 1;
        if (n == 0) return result;

        for (int i = 1; i <= n; ++i) {
            result *= i;
        }

        return result;
    }

    private Graph createGraph(SMCNormalDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double mean = distribution.getMean();
        double stddev = params.get("stddev");
        double variance = Math.pow(stddev, 2);
        
        double coefficient = 1 / Math.sqrt(2 * Math.PI * variance);
        double step = 0.1 * stddev;
        double twoVariance = 2 * variance;
        double negInvTwoVariance = -1 / twoVariance;
    
        double min = Math.max(mean - 3 * stddev, 0);
        double max = mean + 3 * stddev;

        for (double x = min; x <= max; x += step) {
            double exponent = Math.pow(x - mean, 2) * negInvTwoVariance;
            double y = coefficient * Math.exp(exponent);
            points.add(new GraphPoint(x, y));
        }

        return new Graph("Normal Distribution", points, mean);
    }

    private List<Graph> createGraphs(SMCUniformDistribution distribution) {
        List<Graph> graphs = new ArrayList<>(); 
        
        List<GraphPoint> pointsG1 = new ArrayList<>();
        List<GraphPoint> pointsG2 = new ArrayList<>();
        List<GraphPoint> pointsG3 = new ArrayList<>();  

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double a = params.get("a");
        double b = params.get("b");
        double mean = distribution.getMean();

        pointsG1.add(new GraphPoint(0, 0));
        pointsG1.add(new GraphPoint(a, 0));
        pointsG2.add(new GraphPoint(a, 1/(b-a)));
        pointsG2.add(new GraphPoint(b, 1/(b-a)));
        pointsG3.add(new GraphPoint(b, 0));
        pointsG3.add(new GraphPoint(b + a, 0));

        graphs.add(new Graph("Uniform Distribution", pointsG1, mean));
        graphs.add(new Graph("piece2", pointsG2));
        graphs.add(new Graph("piece3", pointsG3));

        return graphs;
    }

    private Graph createGraph(SMCGeometricDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double p = params.get("p");

        double y = p;
        int x = 0;

        while (y > 0.01 && x < 100) {
            points.add(new GraphPoint(x, y));
            y *= (1 - p);
            ++x;
        }

        return new Graph("Geometric distribution", points, distribution.getMean());
    }

    private Graph createGraph(SMCTriangularDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        double mean = distribution.getMean();

        points.add(new GraphPoint(distribution.a, 0));
        points.add(new GraphPoint(distribution.c, 2 / (distribution.b - distribution.a)));
        points.add(new GraphPoint(distribution.b, 0));

        return new Graph("Triangular Distribution", points, mean);
    }

    private Graph createGraph(SMCLogNormalDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        double mean = distribution.getMean();
        double logStddev = distribution.logStddev;
        double logMean = distribution.logMean;

        double min = Math.exp(logMean - 3 * logStddev);
        double max = Math.exp(logMean + 3 * logStddev);
        double step = (max - min) / 1000;

        for (double x = min; x <= max; x += step) {
            double y = distribution.pdf(x);
            points.add(new GraphPoint(x, y));
        }

        return new Graph("Log Normal Distribution", points, mean);
    }

    private Graph createGraph(SMCUserDefinedDistribution distribution) {
        String name = distribution.getName();
        
    	TimedArcPetriNetNetwork network = transition.underlyingTransition().model().parentNetwork();
    	SMCUserDefinedDistribution cd = null;
    	for (SMCUserDefinedDistribution c : network.userDefinedDistributions()) {
    		if (c.getName().equals(name)) {
    			cd = c;
    			break;
    		}
    	}
    	
    	if (cd == null) {
            throw new RequireException("Custom distribution '" + name + "' not found.");
    	}

        List<Double> values = cd.getValues();
        if (values.isEmpty()) {
            throw new RequireException("The distribution contains no values.");
        }
        
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        TreeMap<Double, Integer> frequencies = new TreeMap<>();

        for (double v : values) {
        	sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
            frequencies.put(v, frequencies.getOrDefault(v, 0) + 1);
        }

        List<GraphPoint> points = new ArrayList<>();
        for (Map.Entry<Double, Integer> entry : frequencies.entrySet()) {
            points.add(new GraphPoint(entry.getKey(), (double)entry.getValue() / values.size()));
        }

        return new Graph(distribution.distributionName(), points, sum / values.size());
    }
}
