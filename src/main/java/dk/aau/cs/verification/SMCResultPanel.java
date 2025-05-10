package dk.aau.cs.verification;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.util.MemoryMonitor;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.VerifyTAPN.ObservationData;
import net.tapaal.swinghelpers.GridBagHelper;
import pipe.gui.graph.Graph;
import pipe.gui.graph.DefaultGraphDialog;
import pipe.gui.graph.GraphPoint;
import pipe.gui.graph.MultiGraph;
import pipe.gui.graph.ObservationGraphDialog;
import pipe.gui.graph.GraphDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static net.tapaal.swinghelpers.GridBagHelper.Anchor.WEST;

public class SMCResultPanel extends JPanel  {
    public SMCResultPanel(final VerificationResult<TAPNNetworkTrace> result) {
        setLayout(new GridBagLayout());
        boolean quantitative = result.getQueryResult().isQuantitative();
        SMCStats stats = (SMCStats) result.getStats();

        JPanel resultPanel = createResultPanel(result, stats);
        JPanel statsPanel = createStatsPanel(stats, quantitative);

        JPanel estimates = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = GridBagHelper.as(0, 0, WEST);
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        estimates.add(new JLabel(result.getVerificationTimeString()), gbc);
        gbc = GridBagHelper.as(0, 1, WEST);
        estimates.add(new JLabel("Estimated memory usage: "+ MemoryMonitor.getPeakMemory()), gbc);
        
        if (result.getRawOutput() != null) {
            JButton showRawQueryButton = new JButton("Show raw query results");
            showRawQueryButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(this, createRawQueryPanel(result.getRawOutput()), "Raw query results", JOptionPane.INFORMATION_MESSAGE));
            estimates.add(showRawQueryButton, GridBagHelper.as(1,0, GridBagHelper.Anchor.EAST));
        }

        gbc = GridBagHelper.as(0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(resultPanel, gbc);
        gbc.gridy++;
        add(statsPanel, gbc);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(30,0,0,0);
        add(estimates, gbc);
    }

    private JPanel createResultPanel(final VerificationResult<TAPNNetworkTrace> result, SMCStats stats) {
        QueryResult queryResult = result.getQueryResult();
        JPanel resultPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = GridBagHelper.as(0, 0, new Insets(20, 20, 20, 20));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Query result"));
        resultPanel.add(new JLabel("<html>" + queryResult.toString() + "</html>"), gbc);

        List<Graph> cumulativeGraphs = new ArrayList<>();

        List<GraphPoint> cumulativeDelayPoints = stats.getCumulativeDelayPoints();
        if (!cumulativeDelayPoints.isEmpty()) {
            cumulativeGraphs.add(new Graph("Cumulative Probability / Delay", cumulativeDelayPoints, "Time", "Cumulative Probability", "Time"));
        }

        List<GraphPoint> cumulativeStepPoints = stats.getCumulativeStepPoints();
        if (!cumulativeStepPoints.isEmpty()) {
            cumulativeGraphs.add(new Graph("Cumulative Probability / Step", cumulativeStepPoints, "Number of Steps", "Cumulative Probability", "Step"));
        }

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.gridy = 0;
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.anchor = GridBagConstraints.WEST;
        buttonGbc.insets = new Insets(0, 0, 10, 0);

        if (!cumulativeGraphs.isEmpty()) {
            DefaultGraphDialog.GraphDialogBuilder builder = new DefaultGraphDialog.GraphDialogBuilder();
            GraphDialog graphFrame = builder.addGraphs(cumulativeGraphs).setTitle("SMC Statistics").build();

            String btnText = "Plot cumulative statistics";

            JButton showCumulativeButton = new JButton(btnText);
            showCumulativeButton.addActionListener(arg0 -> graphFrame.display());
            buttonPanel.add(showCumulativeButton, buttonGbc);
            ++buttonGbc.gridy;
        }

        if (!result.getTransitionStatistics().isEmpty()) {
            JButton transitionStatsButton = new JButton("Transition Statistics");
            transitionStatsButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(this, StatisticsPanel.createPanel(result, true), "Transition Statistics", JOptionPane.INFORMATION_MESSAGE));
            buttonPanel.add(transitionStatsButton, buttonGbc);
            ++buttonGbc.gridy;
        }
        
        if (!result.getPlaceBoundStatistics().isEmpty()) {
            JButton placeStatsButton = new JButton("Place-Bound Statistics");
            placeStatsButton.addActionListener(arg0 -> JOptionPane.showMessageDialog(this, StatisticsPanel.createPanel(result, false), "Place-Bound Statistics", JOptionPane.INFORMATION_MESSAGE));
            buttonPanel.add(placeStatsButton, buttonGbc);
            ++buttonGbc.gridy;
        }
        
        buttonGbc.insets = new Insets(0, 0, 0, 0);

        Map<String, ObservationData> observationDataMap = stats.getObservationDataMap();
        if (!observationDataMap.isEmpty()) {
            List<MultiGraph> observationGraphs = new ArrayList<>();

            MultiGraph timeMultiGraph = new MultiGraph("Observation / Time", "Time", "Count", "Time");
            MultiGraph stepMultiGraph = new MultiGraph("Observation / Step", "Step", "Count", "Step");

            for (Map.Entry<String, ObservationData> entry : observationDataMap.entrySet()) {
                String observationName = entry.getKey();
                ObservationData observationData = entry.getValue();
                List<GraphPoint> avgTimePoints = observationData.getSmcObservationAvgTime();
                if (!avgTimePoints.isEmpty()) {
                    timeMultiGraph.addGraph(observationName, "Avg Time", new Graph(avgTimePoints));
                }

                List<GraphPoint> minTimePoints = observationData.getSmcObservationMinTime();
                if (!minTimePoints.isEmpty()) {
                    timeMultiGraph.addGraph(observationName, "Min Time", new Graph(minTimePoints));
                }

                List<GraphPoint> maxTimePoints = observationData.getSmcObservationMaxTime();
                if (!maxTimePoints.isEmpty()) {
                    timeMultiGraph.addGraph(observationName, "Max Time", new Graph(maxTimePoints));
                }

                List<GraphPoint> avgStepPoints = observationData.getSmcObservationAvgStep();
                if (!avgStepPoints.isEmpty()) {
                    stepMultiGraph.addGraph(observationName, "Avg Step", new Graph(avgStepPoints));
                }

                List<GraphPoint> minStepPoints = observationData.getSmcObservationMinStep();
                if (!minStepPoints.isEmpty()) {
                    stepMultiGraph.addGraph(observationName, "Min Step", new Graph(minStepPoints));
                }

                List<GraphPoint> maxStepPoints = observationData.getSmcObservationMaxStep();
                if (!maxStepPoints.isEmpty()) {
                    stepMultiGraph.addGraph(observationName, "Max Step", new Graph(maxStepPoints));
                }

                List<GraphPoint> observationTimePoints = observationData.getSmcObservationValueTime();
                if (!observationTimePoints.isEmpty()) {
                    timeMultiGraph.addGraph(observationName, "Time", new Graph(observationTimePoints));
                }

                List<GraphPoint> observationStepPoints = observationData.getSmcObservationValueStep();
                if (!observationStepPoints.isEmpty()) {
                    stepMultiGraph.addGraph(observationName, "Step", new Graph(observationStepPoints));
                }

                timeMultiGraph.addGlobalAvg(observationName + " Avg Time", observationData.getSmcGlobalAvgTime());
                stepMultiGraph.addGlobalAvg(observationName + " Avg Step", observationData.getSmcGlobalAvgStep());
            }

            if (!timeMultiGraph.isEmpty()) {
                observationGraphs.add(timeMultiGraph);
            }

            if (!stepMultiGraph.isEmpty()) {
                observationGraphs.add(stepMultiGraph);
            }
        
            ObservationGraphDialog.GraphDialogBuilder builder = new ObservationGraphDialog.GraphDialogBuilder();
            GraphDialog graphFrame = builder.addMultiGraphs(observationGraphs)
                                            .setTitle("Observation Statistics")
                                            .showGlobalAverages(true)
                                            .isSimulate(queryResult.getQuery().isSimulate())
                                            .build();
     
            String btnText = "Plot observations";

            JButton showObservationButton = new JButton(btnText);
            buttonPanel.add(showObservationButton, buttonGbc);
            showObservationButton.addActionListener(arg0 -> graphFrame.display());
        }

        resultPanel.add(buttonPanel, GridBagHelper.as(1, 0, WEST, new Insets(0, 0, 10, 0)));

        return resultPanel;
    }

    private JPanel createStatsPanel(SMCStats stats, boolean printRunsStats) {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat verifTimeFormat = new DecimalFormat("#.######", decimalFormatSymbols);
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        JPanel validRunsPanel = new JPanel(new GridBagLayout());
        JPanel violatingRunsPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = GridBagHelper.as(0,0, GridBagHelper.Anchor.WEST, new Insets(10, 0, 10, 30));
        JPanel generalStatsPanel = new JPanel(new GridBagLayout());
        addStatIfPositive(generalStatsPanel, "Executed runs: ", stats.getExecutedRuns(), gbc);
        addStatIfPositive(generalStatsPanel, "Average simulation time per run (ms): ", (1000.0 * stats.getVerificationTime()) / stats.getExecutedRuns(), gbc, verifTimeFormat);
        gbc.gridy = 0;
        gbc.gridx = 3;
        addAvgStdDev(generalStatsPanel, "Average run duration: ", stats.getAverageRunTime(), stats.getRunTimeStdDev(), gbc);
        addAvgStdDev(generalStatsPanel, "Average run length: ", stats.getAverageRunLength(), stats.getRunLengthStdDev(), gbc);

        gbc = GridBagHelper.as(0,0);
        statsPanel.add(generalStatsPanel, gbc);

        validRunsPanel.setBorder(BorderFactory.createTitledBorder("Valid runs satisfying the property"));
        gbc = GridBagHelper.as(0,0, GridBagHelper.Anchor.WEST, new Insets(5, 5, 5, 5));
        if(stats.getValidRuns() == 0) {
            validRunsPanel.add(new JLabel("No valid run"), gbc);
        } else {
            addStatIfPositive(validRunsPanel, "Number: ", stats.getValidRuns(), gbc);
            addAvgStdDev(validRunsPanel, "Average duration: ", stats.getValidRunAverageTime(), stats.getValidRunTimeStdDev(), gbc);
            addAvgStdDev(validRunsPanel, "Average length: ", stats.getValidRunAverageLength(), stats.getValidRunLengthStdDev(), gbc);
        }

        violatingRunsPanel.setBorder(BorderFactory.createTitledBorder("Violating runs not satisfying the property"));
        gbc = GridBagHelper.as(0,0, GridBagHelper.Anchor.WEST, new Insets(5, 5, 5, 5));
        if(stats.getViolatingRuns() == 0) {
            violatingRunsPanel.add(new JLabel("No violating run"), gbc);
        } else {
            addStatIfPositive(violatingRunsPanel, "Number: ", stats.getViolatingRuns(), gbc);
            addAvgStdDev(violatingRunsPanel, "Average duration: ", stats.getViolatingRunAverageTime(), stats.getViolatingRunTimeStdDev(), gbc);
            addAvgStdDev(violatingRunsPanel, "Average length: ", stats.getViolatingRunAverageLength(), stats.getViolatingRunLengthStdDev(), gbc);
        }

        if(printRunsStats) {
            gbc = GridBagHelper.as(0, 1, GridBagHelper.Anchor.WEST, GridBagHelper.Fill.BOTH, new Insets(30,0,0,0));
            JPanel specificStats = new JPanel(new GridLayout());
            specificStats.add(validRunsPanel, gbc);
            specificStats.add(violatingRunsPanel, gbc);
            statsPanel.add(specificStats, gbc);
        }

        return statsPanel;
    }

    private void addAvgStdDev(JPanel panel, String label, double avg, double stdDev, GridBagConstraints gbc) {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#.######", decimalFormatSymbols);
        if(avg < 0) return;
        String toPrint = format.format(avg);
        int x = gbc.gridx;
        panel.add(new JLabel(label), gbc);
        gbc.gridx++;
        panel.add(new JLabel(toPrint), gbc);
        gbc.gridx++;
        if(stdDev >= 0) {
            JLabel sdLabel = new JLabel(" (SD: " + format.format(stdDev) + ")");
            sdLabel.setToolTipText("<html>Standard deviation. <br/>In normal distribution, 68% of all values are one standard deviation from the average, <br/>95% of values within two standard deviations <br/>and 99.99% within three standard deviations from the average.</html>");
            panel.add(sdLabel, gbc);
        }
        gbc.gridx = x;
        gbc.gridy++;
    }

    private void addStatIfPositive(JPanel panel, String label, double stat, GridBagConstraints gbc, DecimalFormat format) {
        if(stat < 0) return;
        int x = gbc.gridx;
        panel.add(new JLabel(label), gbc);
        gbc.gridx++;
        panel.add(new JLabel(format.format(stat)), gbc);
        gbc.gridx = x;
        gbc.gridy++;
    }

    private void addStatIfPositive(JPanel panel, String label, int stat, GridBagConstraints gbc) {
        if(stat < 0) return;
        int x = gbc.gridx;
        panel.add(new JLabel(label), gbc);
        gbc.gridx++;
        panel.add(new JLabel(String.valueOf(stat)), gbc);
        gbc.gridx = x;
        gbc.gridy++;
    }

    private JPanel createRawQueryPanel(final String rawOutput) {
        final JPanel fullPanel = new JPanel(new GridBagLayout());

        JTextArea rawQueryLabel = new JTextArea(rawOutput);
        rawQueryLabel.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(rawQueryLabel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(640,400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        fullPanel.add(scroll, gbc);

        // Make window resizeable
        fullPanel.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                //when the hierarchy changes get the ancestor for the message
                Window window = SwingUtilities.getWindowAncestor(fullPanel);
                //check to see if the ancestor is an instance of Dialog and isn't resizable
                if (window instanceof Dialog) {
                    Dialog dialog = (Dialog) window;
                    dialog.setMinimumSize(dialog.getPreferredSize());
                    if (!dialog.isResizable()) {
                        //set resizable to true
                        dialog.setResizable(true);
                    }
                }
            }
        });

        return fullPanel;
    }
}
