package pipe.gui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.swingx.WrapLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;

public class ObservationGraphDialog extends EscapableDialog implements GraphDialog {
    private final List<MultiGraph> multiGraphs;

    private final Map<String, JCheckBox> observationCheckboxes = new HashMap<>(); 
    private final Map<String, JCheckBox> propertyCheckboxes = new HashMap<>();

    private JFreeChart currentChart;
    private String currentView;

    private ObservationGraphDialog(List<MultiGraph> multiGraphs, String title) {
        super(TAPAALGUI.getAppGui(), title, true);
        this.multiGraphs = multiGraphs;
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
    
        for (String label : new String[]{"Avg", "Min", "Max"}) {
            JCheckBox checkBox = new JCheckBox(label);
            checkBox.setBackground(Color.WHITE);
            checkBox.setSelected(label.equals("Avg"));
            checkBox.addActionListener(e -> updateVisibility());
            propertyCheckboxes.put(label, checkBox);
            buttonPanel.add(checkBox);
        }
    
        southPanel.add(observationPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        currentChart = ((ChartPanel)cardPanel.getComponent(0)).getChart();
        updateVisibility();
    }

    private void updateVisibility() {
        XYPlot plot = currentChart.getXYPlot();
        XYDataset dataset = plot.getDataset();
        
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            String seriesKey = (String)dataset.getSeriesKey(i);
            boolean visible = isSeriesVisible(seriesKey);
            plot.getRenderer().setSeriesVisible(i, visible);
        }
    }

    private boolean isSeriesVisible(String seriesKey) { 
        String[] parts = seriesKey.split(" - ");
        String observation = parts[0];
        String property = parts[1];
        
        boolean obsSelected = observationCheckboxes.get(observation).isSelected();
        boolean propSelected = false;
        
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
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            renderer.setSeriesStroke(i, new BasicStroke(lineThickness));
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        return chart;
    }

    private XYDataset constructDataset(MultiGraph multiGraph) {
        Comparator<String> seriesComparator = (a, b) -> {
            String[] partsA = a.split(" - ");
            String[] partsB = b.split(" - ");
            
            int obsCompare = partsA[0].compareTo(partsB[0]);
            if (obsCompare != 0) return obsCompare;
            
            String propA = partsA[1];
            String propB = partsB[1];
            
            Map<String, Integer> propertyOrder = Map.of(
                "Avg", 1,
                "Min", 2, 
                "Max", 3
            );
            
            String typeA = propA.split(" ")[0];
            String typeB = propB.split(" ")[0];
            
            return propertyOrder.get(typeA) - propertyOrder.get(typeB);
        };

        Map<String, XYSeries> seriesMap = new TreeMap<>(seriesComparator);
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

                seriesMap.put(seriesKey, series);
            }
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (XYSeries series : seriesMap.values()) {
            dataset.addSeries(series);
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
        });

        buttonPanel.add(button);
        buttonPanel.setBackground(Color.WHITE);
    }

    public static class GraphDialogBuilder {
        private List<MultiGraph> multiGraphs = new ArrayList<>();
        private String title = "";
        
        public GraphDialogBuilder addMultiGraphs(List<MultiGraph> multiGraphs) {
            this.multiGraphs.addAll(multiGraphs);
            return this;
        }

        public GraphDialogBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public ObservationGraphDialog build() {
            return new ObservationGraphDialog(multiGraphs, title);
        }
    }
}
