package pipe.gui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.WrapLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;
import java.awt.FlowLayout;

import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;

public class ObservationGraphDialog extends EscapableDialog implements GraphDialog {
    private final List<MultiGraph> multiGraphs;
    private final boolean showGlobalAverages;
    private final boolean isSimulate;

    private final Map<String, JCheckBox> observationCheckboxes = new HashMap<>(); 
    private final Map<String, JCheckBox> propertyCheckboxes = new HashMap<>();

    private DraggableChartPanel currentChartPanel;
    private String currentView;

    private ObservationGraphDialog(List<MultiGraph> multiGraphs, String title, boolean showGlobalAverages, boolean isSimulate) {
        super(TAPAALGUI.getAppGui(), title, true);
        this.multiGraphs = multiGraphs;
        this.showGlobalAverages = showGlobalAverages;
        this.isSimulate = isSimulate;
    }

    @Override
    public void display() {
        if (multiGraphs.isEmpty()) return; 
        resetState();
        displayMultiView();
        setupDialog();
    }

    private void resetState() {
        observationCheckboxes.clear();
        propertyCheckboxes.clear();
        currentChartPanel = null;
        currentView = null;
        getContentPane().removeAll();
    }

    private void displayMultiView() {
        setLayout(new BorderLayout());
 
        var mainPanel = new JPanel(new BorderLayout());
        var cardLayout = new CardLayout();
        var cardPanel = new JPanel(cardLayout);
        var southPanel = new JPanel(new BorderLayout());
        var observationPanel = new JPanel(new WrapLayout(FlowLayout.CENTER));
        var buttonPanel = new JPanel();

        observationPanel.setBackground(Color.WHITE);
        buttonPanel.setBackground(Color.WHITE);
        
        var observationNames = new TreeSet<String>();
        for (var multiGraph : multiGraphs) {
            observationNames.addAll(multiGraph.getMultiGraphMap().keySet());
        }

        var firstObservation = observationNames.iterator().next();
        for (var obsName : observationNames) {
            var obsCheckBox = new JCheckBox(obsName);
            obsCheckBox.setBackground(Color.WHITE);
            obsCheckBox.setSelected(obsName.equals(firstObservation));
            obsCheckBox.addActionListener(e -> updateVisibility());
            observationCheckboxes.put(obsName, obsCheckBox);
            observationPanel.add(obsCheckBox);
        }

        var firstMultiGraph = multiGraphs.get(0);
        currentView = firstMultiGraph.getButtonText();

        for (var multiGraph : multiGraphs) {
            var chart = createChart(multiGraph);
            var chartPanel = new DraggableChartPanel(chart);
            var buttonText = multiGraph.getButtonText();
            cardPanel.add(chartPanel, buttonText);
            addButton(buttonPanel, cardLayout, cardPanel, buttonText);
        }
        
        if (!isSimulate) {
            for (var label : List.of("Avg", "Min", "Max")) {
                var checkBox = new JCheckBox(label);
                checkBox.setBackground(Color.WHITE);
                checkBox.setSelected(label.equals("Avg"));
                checkBox.addActionListener(e -> updateVisibility());
                propertyCheckboxes.put(label, checkBox);
                buttonPanel.add(checkBox);
            }
        }
    
        var observationScrollPane = new JScrollPane(observationPanel);
        observationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        var buttonPanelWrapper = new JPanel(new BorderLayout());
        buttonPanelWrapper.setBackground(Color.WHITE);

        var exportPanel = new JPanel(new BorderLayout());
        var exportButton = new JButton("Export to TikZ");
        exportButton.addActionListener(e -> {
            var currentMultiGraph = getCurrentMultiGraph();
            if (currentMultiGraph != null) {
                var exportGraph = currentMultiGraph.copy();
                var multiGraphMap = exportGraph.getMultiGraphMap();
                multiGraphMap.entrySet().removeIf(entry -> {
                    var observation = entry.getKey();
                    entry.getValue().entrySet().removeIf(propertyEntry -> {
                        var property = propertyEntry.getKey();
                        var seriesKey = observation + " - " + property;
                        return !isSeriesVisible(seriesKey);
                    });

                    return entry.getValue().isEmpty();
                });
               
                GraphExporter.exportToTikz(exportGraph, this, getBaseColors(currentChartPanel.getChart().getXYPlot().getDataset()));
            }
        });

        var resetButton = new JButton("Reset View");
        resetButton.addActionListener(e -> currentChartPanel.resetView());

        var buttonWrapper = new JPanel();
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(resetButton);
        buttonWrapper.add(exportButton);
        
        exportPanel.add(buttonWrapper, BorderLayout.EAST);
        exportPanel.setBackground(Color.WHITE);
        
        buttonPanelWrapper.add(buttonPanel, BorderLayout.CENTER);
        buttonPanelWrapper.add(exportPanel, BorderLayout.SOUTH);

        southPanel.add(observationScrollPane, BorderLayout.NORTH);
        southPanel.add(buttonPanelWrapper, BorderLayout.SOUTH);
        
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        currentChartPanel = (DraggableChartPanel)cardPanel.getComponent(0);
        updateVisibility();

        var plot = currentChartPanel.getChart().getXYPlot();
        plot.setFixedLegendItems(createCustomLegendItems(plot.getDataset(), multiGraphs.get(0)));
    }

    private void updateVisibility() {
        var plot = currentChartPanel.getChart().getXYPlot();
        var dataset = plot.getDataset();
        var renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        var lineThickness = 3.0f;

        for (var i = 0; i < dataset.getSeriesCount(); ++i) {
            var seriesKey = (String)dataset.getSeriesKey(i);
            var visible = isSeriesVisible(seriesKey);
    
            renderer.setSeriesVisible(i, visible);
            renderer.setSeriesStroke(i, createStrokeForSeries(seriesKey, lineThickness));
        }

        renderer.setDrawSeriesLineAsPath(true);

        setSeriesColors(dataset, renderer);
    }

    private Map<String, Color> getBaseColors(XYDataset dataset) {
        var baseColors = new HashMap<String, Color>();
        var uniqueBaseNames = new HashSet<String>();
        for (var i = 0; i < dataset.getSeriesCount(); ++i) {
            var seriesKey = (String)dataset.getSeriesKey(i);
            var baseName = seriesKey.split(" - ")[0];
            uniqueBaseNames.add(baseName);
        }

        var colorGenerator = new ColorGenerator();
        for (var baseName : uniqueBaseNames) {
            baseColors.put(baseName, colorGenerator.nextColor());
        }

        return baseColors;
    }

    private BasicStroke createStrokeForSeries(String seriesKey, float lineThickness) {
        if (seriesKey.contains("Min")) {
            return new BasicStroke(lineThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 6.0f}, 0.0f);
        } else if (seriesKey.contains("Max")) {
            return new BasicStroke(lineThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f, 2.0f}, 0.0f);
        } else {
            return new BasicStroke(lineThickness);
        }
    }

    private boolean isSeriesVisible(String seriesKey) { 
        var parts = seriesKey.split(" - ");
        var observation = parts[0];
        var property = parts[1];
        
        var obsSelected = observationCheckboxes.get(observation).isSelected();
        var propSelected = false;
        
        if (isSimulate) return obsSelected;

        var currentView = getCurrentView();
        for (var entry : propertyCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                var expectedProperty = entry.getKey() + " " + currentView;
                if (property.equals(expectedProperty)) {
                    propSelected = true;
                    break;
                }
            }
        }
        
        return obsSelected && propSelected;
    }

    private String getCurrentView() {
        return currentView;
    }

    private MultiGraph getCurrentMultiGraph() {
        return multiGraphs.stream()
                .filter(multiGraph -> multiGraph.getButtonText().equals(getCurrentView()))
                .findFirst()
                .orElse(null);
    }

    private JFreeChart createChart(MultiGraph multiGraph) {
        var dataset = constructDataset(multiGraph);
        var chart = ChartFactory.createXYLineChart(multiGraph.getName(), multiGraph.getXAxisLabel(), multiGraph.getYAxisLabel(), dataset);

        var plot = chart.getXYPlot();
        var lineThickness = 3.0f;

        var renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        for (var i = 0; i < dataset.getSeriesCount(); ++i) {
            var seriesKey = (String)dataset.getSeriesKey(i);
            renderer.setSeriesStroke(i, createStrokeForSeries(seriesKey, lineThickness));
        }

        setSeriesColors(dataset, renderer);

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        return chart;
    }

    private void setSeriesColors(XYDataset dataset, XYLineAndShapeRenderer renderer) {
        var baseColors = getBaseColors(dataset);
        for (var i = 0; i < dataset.getSeriesCount(); ++i) {
            var seriesKey = (String)dataset.getSeriesKey(i);
            var baseName = seriesKey.split(" - ")[0];
            renderer.setSeriesPaint(i, baseColors.get(baseName));
        }
    }

    private LegendItemCollection createCustomLegendItems(XYDataset dataset, MultiGraph multiGraph) {
        var legendItems = new LegendItemCollection();
        var baseColors = getBaseColors(dataset);

        for (var i = 0; i < dataset.getSeriesCount(); ++i) {
            var seriesKey = (String)dataset.getSeriesKey(i);
            var baseName = seriesKey.split(" - ")[0];

            if (seriesKey.contains("Avg") && seriesKey.contains(getCurrentView())) {
                var color = baseColors.get(baseName);
                var legendText = baseName;
    
                var globalAvgMap = multiGraph.getMultiGraphGlobalAvgMap();
                var key = baseName + " Avg " + getCurrentView();
                if (globalAvgMap.containsKey(key) && showGlobalAverages) {
                    legendText += " (avg=" + globalAvgMap.get(key) + ")";
                }

                var legendItem = new LegendItem(legendText, color);
                legendItems.add(legendItem);
            }
        }
    
        return legendItems;
    }

    private XYDataset constructDataset(MultiGraph multiGraph) {
        var dataset = new XYSeriesCollection();
        for (var entry : multiGraph.getMultiGraphMap().entrySet()) {
            var observationName = entry.getKey();
            var propertyGraphs = entry.getValue();
            for (var propertyGraph : propertyGraphs.entrySet()) {
                var property = propertyGraph.getKey();
                var graph = propertyGraph.getValue();
            
                var seriesKey = observationName + " - " + property;
                var series = new XYSeries(seriesKey);
                for (var point : graph.getPoints()) {
                    series.add(point.getX(), point.getY());
                }
 
                dataset.addSeries(series);
            }
        }

        return dataset;
    }

    private void setupDialog() {
        setSize(800, 600);
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private void addButton(JPanel buttonPanel, CardLayout cardLayout, JPanel cardPanel, String buttonText) {
        var button = new JButton(buttonText);
        button.addActionListener(e -> {
            cardLayout.show(cardPanel, buttonText);
            currentView = buttonText;

            for (var component : cardPanel.getComponents()) {
                if (component instanceof DraggableChartPanel chartPanel && component.isVisible()) {
                    currentChartPanel = chartPanel;
                    break;
                }
            }

            updateVisibility();

            var plot = currentChartPanel.getChart().getXYPlot();
            plot.setFixedLegendItems(createCustomLegendItems(plot.getDataset(), getCurrentMultiGraph()));
        });

        buttonPanel.add(button);
        buttonPanel.setBackground(Color.WHITE);
    }

    public static class GraphDialogBuilder {
        private List<MultiGraph> multiGraphs = new ArrayList<>();
        private String title = "";
        private boolean showGlobalAverages;
        private boolean isSimulate;
        
        public GraphDialogBuilder addMultiGraphs(List<MultiGraph> multiGraphs) {
            this.multiGraphs.addAll(multiGraphs);
            return this;
        }

        public GraphDialogBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public GraphDialogBuilder showGlobalAverages(boolean showGlobalAverages) {
            this.showGlobalAverages = true;
            return this;
        }

        public GraphDialogBuilder isSimulate(boolean isSimulate) {
            this.isSimulate = isSimulate;
            return this;
        }

        public ObservationGraphDialog build() {
            return new ObservationGraphDialog(multiGraphs, title, showGlobalAverages, isSimulate);
        }
    }
}
