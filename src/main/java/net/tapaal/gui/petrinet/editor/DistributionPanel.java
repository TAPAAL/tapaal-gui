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
import pipe.gui.petrinet.graphicElements.tapn.TimedTransitionComponent;
import pipe.gui.swingcomponents.EscapableDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

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
        distributionShowGraph = new JButton("Show graph");
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
        gbc.gridx += 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(distributionShowGraph, gbc);
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

    private void showDistributionGraph() {
        SMCDistribution distribution = parseDistribution();

        try {
            GraphDialog dialog = createGraphDialog(distribution);
            dialog.display();
        } catch (RequireException e) {
            JOptionPane.showMessageDialog(TAPAALGUI.getApp(), "There was an error opening the graph. Reason: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private GraphDialog createGraphDialog(SMCDistribution distribution) {
        String frameTitle = "Distribution graph";
        
        if (distribution instanceof SMCConstantDistribution) {
            Graph graph = createGraph((SMCConstantDistribution) distribution);
            return new GraphDialog(graph, frameTitle);
        } else if (distribution instanceof SMCDiscreteUniformDistribution) {
            Graph graph = createGraph((SMCDiscreteUniformDistribution) distribution);
            return new GraphDialog(graph, frameTitle);
        } else if (distribution instanceof SMCExponentialDistribution) {
            Graph graph = createGraph((SMCExponentialDistribution) distribution);
            return new GraphDialog(graph, frameTitle);
        } else if (distribution instanceof SMCGammaDistribution) {
            Graph graph = createGraph((SMCGammaDistribution) distribution);
            return new GraphDialog(graph, frameTitle);
        } else if (distribution instanceof SMCNormalDistribution) {
            Graph graph = createGraph((SMCNormalDistribution) distribution);
            return new GraphDialog(graph, frameTitle);
        } else if (distribution instanceof SMCUniformDistribution) {
            List<Graph> graphs = createGraphs((SMCUniformDistribution) distribution);
            return new GraphDialog(graphs, frameTitle, false, true);
        }

        return null;
    }

    private Graph createGraph(SMCConstantDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();
        LinkedHashMap<String, Double> params = distribution.getParameters();
        double value = params.get("value");
        
        points.add(new GraphPoint(value, 0));
        points.add(new GraphPoint(value, 100));

        return new Graph("Constant Distribution", points);
    }

    private Graph createGraph(SMCDiscreteUniformDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();
        return new Graph("Discrete Uniform Distribution", points);
    }

    private Graph createGraph(SMCExponentialDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double rate = params.get("rate");

        for (int x = 0; x <= 100; x++) {
            points.add(new GraphPoint(x, rate * Math.exp(-rate * x)));
        }

        return new Graph("Exponential Distribution", points);
    }

    private Graph createGraph(SMCGammaDistribution distribution) {
        List<GraphPoint> points = new ArrayList<>();

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double shape = params.get("shape");
        double scale = params.get("scale");

        Require.that(shape > 0, "Shape must be a non-negative real");

        double gamma = spougeGammaApprox(shape - 1);
        double coefficient = 1 / (gamma * Math.pow(scale, shape));
        double step = 0.1;
        for (double x = Double.MIN_VALUE; x <= 20; x += step) {
            double y = coefficient * Math.pow(x, shape - 1) * Math.exp(-(x / scale));
            points.add(new GraphPoint(x, y));
        }

        return new Graph("Gamma Distribution", points);
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
        double mean = params.get("mean");
        double stddev = params.get("stddev");
        double variance = Math.pow(stddev, 2);
        
        double coefficient = 1 / Math.sqrt(2 * Math.PI * variance);
        double step = 0.1 * stddev;
        double twoVariance = 2 * variance;
        double negInvTwoVariance = -1 / twoVariance;
        
        double min = mean - 3 * stddev;
        double max = mean + 3 * stddev;

        for (double x = min; x <= max; x += step) {
            double exponent = Math.pow(x - mean, 2) * negInvTwoVariance;
            double y = coefficient * Math.exp(exponent);
            points.add(new GraphPoint(x, y));
        }

        return new Graph("Normal Distribution", points);
    }

    private List<Graph> createGraphs(SMCUniformDistribution distribution) {
        List<Graph> graphs = new ArrayList<>(); 
        
        List<GraphPoint> pointsG1 = new ArrayList<>();
        List<GraphPoint> pointsG2 = new ArrayList<>();
        List<GraphPoint> pointsG3 = new ArrayList<>();  

        LinkedHashMap<String, Double> params = distribution.getParameters();
        double a = params.get("a");
        double b = params.get("b");

        pointsG1.add(new GraphPoint(0, 0));
        pointsG1.add(new GraphPoint(a, 0));
        pointsG2.add(new GraphPoint(a, 1/(b-a)));
        pointsG2.add(new GraphPoint(b, 1/(b-a)));
        pointsG3.add(new GraphPoint(b, 0));
        pointsG3.add(new GraphPoint(b + a, 0));

        graphs.add(new Graph("Uniform Distribution", pointsG1));
        graphs.add(new Graph("piece2", pointsG2));
        graphs.add(new Graph("piece3", pointsG3));

        return graphs;
    }

    private JRadioButton useContinuousDistribution;
    private JRadioButton useDiscreteDistribution;
    private ButtonGroup distributionCategoryGroup;
    private JComboBox<String> distributionType;
    private JButton distributionShowGraph;
    private JLabel distributionParam1Label;
    private JLabel distributionParam2Label;
    private JTextField distributionParam1Field;
    private JTextField distributionParam2Field;
    private JLabel distributionExplanation;

}
