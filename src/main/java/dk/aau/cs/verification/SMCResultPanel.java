package dk.aau.cs.verification;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.util.MemoryMonitor;
import net.tapaal.swinghelpers.GridBagHelper;
import pipe.gui.graph.Graph;
import pipe.gui.graph.GraphDialog;
import pipe.gui.graph.GraphPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import static net.tapaal.swinghelpers.GridBagHelper.Anchor.WEST;

public class SMCResultPanel extends JPanel  {

    public SMCResultPanel(final VerificationResult<TAPNNetworkTrace> result) {
        setLayout(new GridBagLayout());
        boolean quantitative = result.getQueryResult().isQuantitative();
        SMCStats stats = (SMCStats) result.getStats();

        JPanel resultPanel = createResultPanel(result.getQueryResult(), stats);
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

    private JPanel createResultPanel(QueryResult result, SMCStats stats) {
        JPanel resultPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = GridBagHelper.as(0, 0, new Insets(20, 20, 20, 20));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Query result"));
        resultPanel.add(new JLabel("<html>" + result.toString() + "</html>"), gbc);

        java.util.List<Graph> graphs = new ArrayList<>();

        java.util.List<GraphPoint> cumulativeDelayPoints = stats.getCumulativeDelayPoints();
        if (!cumulativeDelayPoints.isEmpty()) {
            graphs.add(new Graph("Cumulative Probability / Delay", cumulativeDelayPoints, "Time", "Cumulative Probability", "Delay"));
        }

        List<GraphPoint> cumulativeStepPoints = stats.getCumulativeStepPoints();
        if (!cumulativeStepPoints.isEmpty()) {
            graphs.add(new Graph("Cumulative Probability / Step", cumulativeStepPoints, "Number of Steps", "Cumulative Probability", "Step"));
        }

        if (!graphs.isEmpty()) {
            GraphDialog.GraphDialogBuilder builder = new GraphDialog.GraphDialogBuilder();
            GraphDialog graphFrame = builder.addGraphs(graphs).setTitle("SMC Statistics").build();

            String btnText = "Plot cumulative statistics";

            JButton showGraphButton = new JButton(btnText);
            gbc = GridBagHelper.as(1, 0, WEST, new Insets(0,0,10,0));
            resultPanel.add(showGraphButton, gbc);
            showGraphButton.addActionListener(arg0 -> graphFrame.display());
        }

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
        addStatIfPositive(generalStatsPanel, "Verification time / run (ms): ", (1000.0 * stats.getVerificationTime()) / stats.getExecutedRuns(), gbc, verifTimeFormat);
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

    private void addStatIfPositive(JPanel panel, String label, double stat, GridBagConstraints gbc) {
        if(stat < 0) return;
        int x = gbc.gridx;
        panel.add(new JLabel(label), gbc);
        gbc.gridx++;
        panel.add(new JLabel(String.valueOf(stat)), gbc);
        gbc.gridx = x;
        gbc.gridy++;
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
