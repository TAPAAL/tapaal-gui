package pipe.gui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.WrapLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Paint;

import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;

public class ObservationGraphDialog extends EscapableDialog implements GraphDialog {
    private final List<MultiGraph> multiGraphs;
    private final boolean showGlobalAverages;
    private final boolean isSimulate;

    private final Map<String, JCheckBox> observationCheckboxes = new HashMap<>(); 
    private final Map<String, JCheckBox> propertyCheckboxes = new HashMap<>();

    private JFreeChart currentChart;
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
        currentChart = null;
        currentView = null;
        getContentPane().removeAll();
    }

    private void displayMultiView() {
        setLayout(new BorderLayout());
 
        JPanel mainPanel = new JPanel(new BorderLayout());
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        JPanel southPanel = new JPanel(new BorderLayout());
        JPanel observationPanel = new JPanel(new WrapLayout(FlowLayout.CENTER));
        JPanel buttonPanel = new JPanel();

        observationPanel.setBackground(Color.WHITE);
        buttonPanel.setBackground(Color.WHITE);
        
        Set<String> observationNames = new TreeSet<>();
        for (MultiGraph multiGraph : multiGraphs) {
            observationNames.addAll(multiGraph.getMultiGraphMap().keySet());
        }

        String firstObservation = observationNames.iterator().next();
        for (String obsName : observationNames) {
            JCheckBox obsCheckBox = new JCheckBox(obsName);
            obsCheckBox.setBackground(Color.WHITE);
            obsCheckBox.setSelected(obsName.equals(firstObservation));
            obsCheckBox.addActionListener(e -> updateVisibility());
            observationCheckboxes.put(obsName, obsCheckBox);
            observationPanel.add(obsCheckBox);
        }

        MultiGraph firstMultiGraph = multiGraphs.get(0);
        currentView = firstMultiGraph.getButtonText();

        for (MultiGraph multiGraph : multiGraphs) {
            JFreeChart chart = createChart(multiGraph);
            ChartPanel chartPanel = createChartPanel(chart);
            String buttonText = multiGraph.getButtonText();
            cardPanel.add(chartPanel, buttonText);
            addButton(buttonPanel, cardLayout, cardPanel, buttonText);
        }
        
        if (!isSimulate) {
            for (String label : List.of("Avg", "Min", "Max")) {
                JCheckBox checkBox = new JCheckBox(label);
                checkBox.setBackground(Color.WHITE);
                checkBox.setSelected(label.equals("Avg"));
                checkBox.addActionListener(e -> updateVisibility());
                propertyCheckboxes.put(label, checkBox);
                buttonPanel.add(checkBox);
            }
        }
    
        JScrollPane observationScrollPane = new JScrollPane(observationPanel);
        observationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        southPanel.add(observationScrollPane, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        currentChart = ((ChartPanel)cardPanel.getComponent(0)).getChart();
        updateVisibility();

        XYPlot plot = currentChart.getXYPlot();
        XYDataset dataset = plot.getDataset();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        plot.setFixedLegendItems(createCustomLegendItems(dataset, renderer, multiGraphs.get(0)));
    }

    private void updateVisibility() {
        XYPlot plot = currentChart.getXYPlot();
        XYDataset dataset = plot.getDataset();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        float lineThickness = 3.0f;

        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            boolean visible = isSeriesVisible(seriesKey);
    
            renderer.setSeriesVisible(i, visible);
            renderer.setSeriesStroke(i, createStrokeForSeries(seriesKey, lineThickness));
        }

        renderer.setDrawSeriesLineAsPath(true);

        Map<String, Paint> baseColors = getBaseColors(dataset, renderer);
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            String baseName = seriesKey.split(" - ")[0];
            if (baseColors.containsKey(baseName)) {
                renderer.setSeriesPaint(i, baseColors.get(baseName));
            }
        }
    }

    private Map<String, Paint> getBaseColors(XYDataset dataset, XYLineAndShapeRenderer renderer) {
        Map<String, Paint> baseColors = new HashMap<>();
        Set<String> uniqueBaseNames = new HashSet<>();
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            String baseName = seriesKey.split(" - ")[0];
            uniqueBaseNames.add(baseName);
        }

        ColorGenerator colorGenerator = new ColorGenerator();
        for (String baseName : uniqueBaseNames) {
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
        String[] parts = seriesKey.split(" - ");
        String observation = parts[0];
        String property = parts[1];
        
        boolean obsSelected = observationCheckboxes.get(observation).isSelected();
        boolean propSelected = false;
        
        if (isSimulate) return obsSelected;

        String currentView = getCurrentView();
        for (Map.Entry<String, JCheckBox> entry : propertyCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                String expectedProperty = entry.getKey() + " " + currentView;
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

    private DraggableChartPanel createChartPanel(JFreeChart chart) {
        DraggableChartPanel chartPanel = new DraggableChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        return chartPanel;
    }

    private JFreeChart createChart(MultiGraph multiGraph) {
        XYDataset dataset = constructDataset(multiGraph);
        JFreeChart chart = ChartFactory.createXYLineChart(multiGraph.getName(), multiGraph.getXAxisLabel(), multiGraph.getYAxisLabel(), dataset);

        XYPlot plot = chart.getXYPlot();
        float lineThickness = 3.0f;

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            renderer.setSeriesStroke(i, createStrokeForSeries(seriesKey, lineThickness));
        }

        Map<String, Paint> baseColors = getBaseColors(dataset, renderer);
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            String baseName = seriesKey.split(" - ")[0];
            if (baseColors.containsKey(baseName)) {
                renderer.setSeriesPaint(i, baseColors.get(baseName));
            }
        };

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        return chart;
    }

    private LegendItemCollection createCustomLegendItems(XYDataset dataset, XYLineAndShapeRenderer renderer, MultiGraph multiGraph) {
        LegendItemCollection legendItems = new LegendItemCollection();
        Map<String, Paint> baseColors = new HashMap<>();
        Set<String> uniqueBaseNames = new HashSet<>();
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            String baseName = seriesKey.split(" - ")[0];
            uniqueBaseNames.add(baseName);
        }

        ColorGenerator colorGenerator = new ColorGenerator();
        for (String baseName : uniqueBaseNames) {
            baseColors.put(baseName, colorGenerator.nextColor());
        }

        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            String baseName = seriesKey.split(" - ")[0];

            if (seriesKey.contains("Avg") && seriesKey.contains(getCurrentView())) {
                Paint paint = baseColors.get(baseName);
                String legendText = baseName;
    
                Map<String, Double> globalAvgMap = multiGraph.getMultiGraphGlobalAvgMap();
                String key = baseName + " Avg " + getCurrentView();
                if (globalAvgMap.containsKey(key) && showGlobalAverages) {
                    legendText += " (avg=" + globalAvgMap.get(key) + ")";
                }

                LegendItem legendItem = new LegendItem(legendText, paint);
                legendItems.add(legendItem);
            }
        }
    
        return legendItems;
    }

    private XYDataset constructDataset(MultiGraph multiGraph) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (Map.Entry<String, Map<String, Graph>> entry : multiGraph.getMultiGraphMap().entrySet()) {
            String observationName = entry.getKey();
            Map<String, Graph> propertyGraphs = entry.getValue();
            for (Map.Entry<String, Graph> propertyGraph : propertyGraphs.entrySet()) {
                String property = propertyGraph.getKey();
                Graph graph = propertyGraph.getValue();
            
                String seriesKey = observationName + " - " + property;
                XYSeries series = new XYSeries(seriesKey);
                for (GraphPoint point : graph.getPoints()) {
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
        JButton button = new JButton(buttonText);
        button.addActionListener(e -> {
            cardLayout.show(cardPanel, buttonText);
            currentView = buttonText;

            for (Component comp : cardPanel.getComponents()) {
                if (comp instanceof ChartPanel && comp.isVisible()) {
                    currentChart = ((ChartPanel)comp).getChart();
                    break;
                }
            }

            updateVisibility();

            XYPlot plot = currentChart.getXYPlot();
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
            plot.setFixedLegendItems(createCustomLegendItems(plot.getDataset(), renderer, getCurrentMultiGraph()));
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
