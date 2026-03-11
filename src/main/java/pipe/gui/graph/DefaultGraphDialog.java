package pipe.gui.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dk.aau.cs.util.Require;
import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;

public class DefaultGraphDialog extends EscapableDialog implements GraphDialog {
    private final List<Graph> graphs;
    
    private boolean showLegend;
    private boolean piecewise;
    private boolean pointPlot;
    private boolean isStraight;
    private double distanceToOrigin;
    private Double mean;
    private int k;

    private boolean hasZeroX;
    private boolean hasZeroY;

    private String currentCard = "";
    private final List<ChartPanel> chartPanels = new ArrayList<>();
    private JFreeChart singleChart;

    private DefaultGraphDialog(List<Graph> graphs, String title, boolean showLegend, boolean piecewise, boolean pointPlot) {
        super(TAPAALGUI.getAppGui(), title, true);
        this.graphs = graphs;
        this.showLegend = showLegend;
        this.piecewise = piecewise;
        this.pointPlot = pointPlot;
        this.k = -1;
    }

    @Override
    public void display() {
        if (piecewise || graphs.size() < 2) {
            displayWithoutButtons();
        } else {
            displayWithButtons();
        }

        setupDialog();
    }

    private void displayWithoutButtons() {
        JFreeChart chart = createChart(graphs);
        singleChart = chart;
        ChartPanel chartPanel = createChartPanel(chart);

        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        JPanel exportPanel = createExportPanel(e -> {
            if (piecewise) {
                GraphExporter.exportPiecewiseToTikz(graphs, this);
            } else if (pointPlot) {
                GraphExporter.exportPointPlotToTikz(graphs.get(0), this);
            } else {
                GraphExporter.exportToTikz(graphs.get(0), this);
            }
        });
        
        add(exportPanel, BorderLayout.SOUTH);
    }

    private void displayWithButtons() {
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        JPanel buttonPanel = new JPanel();

        for (Graph graph : graphs) {
            JFreeChart chart = createChart(Collections.singletonList(graph));
            chartPanels.add(createChartPanel(chart));
            ChartPanel chartPanel = chartPanels.get(chartPanels.size() - 1);

            String buttonText = graph.getButtonText();
            currentCard = buttonText;
            cardPanel.add(chartPanel, buttonText);

            JButton button = new JButton(buttonText);
            button.addActionListener(e -> {
                cardLayout.show(cardPanel, buttonText);
                currentCard = buttonText;
            });

            buttonPanel.add(button);
        }

        currentCard = graphs.get(0).getButtonText();

        JPanel exportPanel = createExportPanel(e -> {
            Graph currentGraph = graphs.stream()
                .filter(g -> g.getButtonText().equals(currentCard))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No graph found for current card: " + currentCard));
            GraphExporter.exportToTikz(currentGraph, DefaultGraphDialog.this);
        });

        JPanel southPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(Color.WHITE);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(exportPanel, BorderLayout.SOUTH);
        southPanel.setBackground(Color.WHITE);
    
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createExportPanel(ActionListener exportAction) {
        JPanel exportPanel = new JPanel(new BorderLayout());
        JButton exportButton = new JButton("Export to TikZ");
        exportButton.addActionListener(exportAction);
        
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(exportButton);

        if (pointPlot) {
            JPanel sliderPanel = new JPanel();
            sliderPanel.setBackground(Color.WHITE);
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
            JLabel label = new JLabel("Bins: Max");
            label.setPreferredSize(new Dimension(80, 20));
            
            slider.addChangeListener(e -> {
                int maxPoints = graphs.stream().mapToInt(g -> g.getPoints().size()).max().orElse(1);
                int maxBinsAllowed = Math.max(1, maxPoints - 1);
                
                int value = ((JSlider) e.getSource()).getValue();
                if (value == 100) {
                    k = -1;
                    label.setText("Bins: Max");
                } else {
                    int bins = Math.max(1, (int)(value * maxBinsAllowed / 100.0));
                    k = bins;
                    label.setText("Bins: " + k);
                }
                updateDataset();
            });
            sliderPanel.add(label);
            sliderPanel.add(slider);
            
            exportPanel.add(sliderPanel, BorderLayout.WEST);
        }
        exportPanel.add(buttonWrapper, BorderLayout.EAST);
        exportPanel.setBackground(Color.WHITE);

        return exportPanel;
    }

    private void setupDialog() {
        setSize(800, 600);
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private DraggableChartPanel createChartPanel(JFreeChart chart) {
        DraggableChartPanel chartPanel = new DraggableChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        return chartPanel;
    }

    private boolean isBinningEnabled(List<Graph> graphList) {
        return k > 0 && graphList.stream().anyMatch(g -> g.getPoints().size() > k);
    }

    private JFreeChart createChart(List<Graph> graphs) {
        XYDataset dataset = constructDataset(graphs);
        JFreeChart chart;
        if (graphs.isEmpty()) {
            chart = ChartFactory.createXYLineChart("", "X", "Y", dataset, PlotOrientation.VERTICAL, false, true, false);
        } else {
            chart = ChartFactory.createXYLineChart(graphs.get(0).getName(), graphs.get(0).getXAxisLabel(), graphs.get(0).getYAxisLabel(), dataset, PlotOrientation.VERTICAL, showLegend || mean != null, true, false);
        }

        XYPlot plot = chart.getXYPlot();
        float lineThickness = 3.0f;

        final double negativeMargin = -0.01;
        if (hasZeroX) {
            ValueAxis domainAxis = plot.getDomainAxis();
            domainAxis.setRange(negativeMargin, domainAxis.getUpperBound());
        }
        
        if (hasZeroY) {
            ValueAxis rangeAxis = plot.getRangeAxis();
            rangeAxis.setRange(negativeMargin, rangeAxis.getUpperBound());
        }

        if (isStraight) {
            ValueAxis domainAxis = plot.getDomainAxis();
            domainAxis.setRange(distanceToOrigin - 1, distanceToOrigin + 1);
        } else if (mean != null) {
            ValueAxis rangeAxis = plot.getRangeAxis();
            float[] dashPattern = { 5.0f, 5.0f };
            BasicStroke dashed = new BasicStroke(lineThickness, 
                                                BasicStroke.CAP_BUTT,
                                                BasicStroke.JOIN_MITER, 
                                                10.0f, 
                                                dashPattern,
                                                0.0f);

            XYLineAnnotation annotation = new XYLineAnnotation(mean, rangeAxis.getLowerBound(), mean, rangeAxis.getUpperBound(), dashed, Color.BLACK);
            plot.addAnnotation(annotation);

            Shape lineShape = new Line2D.Double(0, 0, 30, 0);
            LegendItemCollection legendItems;
            
            legendItems = showLegend ? plot.getLegendItems() : new LegendItemCollection();
            legendItems.add(new LegendItem("Mean", null, null, null, lineShape, Color.BLACK, dashed, Color.BLACK));
            plot.setFixedLegendItems(legendItems);
        }

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        Color lineColor = Color.RED;
        
        boolean usesBinning = isBinningEnabled(graphs);

        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            renderer.setSeriesStroke(i, new BasicStroke(lineThickness));
            renderer.setSeriesShapesVisible(i, pointPlot);
            renderer.setSeriesLinesVisible(i, !pointPlot || usesBinning);
            renderer.setSeriesPaint(i, lineColor);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        return chart;
    }

    private void updateDataset() {
        if (singleChart != null) {
            boolean usesBinning = isBinningEnabled(graphs);
            updatePlotDataset(singleChart.getXYPlot(), constructDataset(graphs), usesBinning);
        } else {
            for (int i = 0; i < Math.min(graphs.size(), chartPanels.size()); ++i) {
                List<Graph> singleGraphList = Collections.singletonList(graphs.get(i));
                boolean usesBinning = isBinningEnabled(singleGraphList);
                updatePlotDataset(chartPanels.get(i).getChart().getXYPlot(), constructDataset(singleGraphList), usesBinning);
            }
        }
    }

    private void updatePlotDataset(XYPlot plot, XYDataset dataset, boolean usesBinning) {
        plot.setDataset(dataset);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            renderer.setSeriesLinesVisible(i, !pointPlot || usesBinning);
        }
    }

    private XYDataset constructDataset(List<Graph> graphs) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        hasZeroX = false;
        hasZeroY = false;
        
        for (Graph graph : graphs) {
            XYSeries series = new XYSeries(graph.getName());
            List<GraphPoint> points = graph.getPoints();

            if (k > 0 && points.size() > k) {
                points = binPoints(points, k);
            }

            double margin = 1e-5;
            if (!points.isEmpty()) {
                double first = points.get(0).getX();
                double last = points.get(points.size() - 1).getX();

                isStraight = Math.abs(first - last) < margin && !piecewise;
                distanceToOrigin = first;
            }

            if (graph.getMean() != null) {
                mean = graph.getMean();
            }

            for (GraphPoint point : points) {
                Require.that(point.getX() >= 0 && point.getY() >= 0, "Negative points are not supported");

                series.add(point.getX(), point.getY());

                hasZeroX |= point.getX() < margin;
                hasZeroY |= point.getY() < margin;
            }
            
            dataset.addSeries(series);
        }
        
        return dataset;
    }

    private List<GraphPoint> binPoints(List<GraphPoint> points, int binCount) {
        if (points.isEmpty()) return points;

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        for (GraphPoint p : points) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
        }

        if (minX == maxX) return points;

        double binWidth = (maxX - minX) / binCount;
        List<GraphPoint> binned = new ArrayList<>();

        double[] binSumsY = new double[binCount];
        double[] binSumsX = new double[binCount];
        int[] binCounts = new int[binCount];

        for (GraphPoint p : points) {
            int binIndex = (int)((p.getX() - minX) / binWidth);
            if (binIndex >= binCount) binIndex = binCount - 1;
            if (binIndex < 0) binIndex = 0;

            binSumsX[binIndex] += p.getX();
            binSumsY[binIndex] += p.getY();
            ++binCounts[binIndex];
        }

        // Create binned points (using avgX, avgY)
        for (int i = 0; i < binCount; ++i) {
            if (binCounts[i] > 0) {
                double avgX = binSumsX[i] / binCounts[i];
                double avgY = binSumsY[i] / binCounts[i];
                binned.add(new GraphPoint(avgX, avgY));
            }
        }

        // Normalize so sum of Y = 1 (if total > 0)
        double totalY = 0.0;
        for (GraphPoint gp : binned) {
            totalY += gp.getY();
        }
        if (totalY > 0) {
            for (int i = 0; i < binned.size(); ++i) {
                GraphPoint old = binned.get(i);
                binned.set(i, new GraphPoint(old.getX(), old.getY() / totalY));
            }
        }

        return binned;
    }

    public static class GraphDialogBuilder {
        private List<Graph> graphs = new ArrayList<>();
    
        private String title;
        private boolean showLegend;
        private boolean piecewise;
        private boolean pointPlot;

        public GraphDialogBuilder addGraphs(List<Graph> graphs) {
            this.graphs.addAll(graphs);
            return this;
        }

        public GraphDialogBuilder addGraph(Graph graph) {
            this.graphs.add(graph);
            return this;
        }

        public GraphDialogBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public GraphDialogBuilder setShowLegend(boolean showLegend) {
            this.showLegend = showLegend;
            return this;
        }

        public GraphDialogBuilder setPiecewise(boolean piecewise) {
            this.piecewise = piecewise;
            return this;
        }

        public GraphDialogBuilder setPointPlot(boolean pointPlot) {
            this.pointPlot = pointPlot;
            return this;
        }

        public GraphDialog build() {
            return new DefaultGraphDialog(graphs, title, showLegend, piecewise, pointPlot);
        }
    }
}