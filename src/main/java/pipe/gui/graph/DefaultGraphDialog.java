package pipe.gui.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Line2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

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

    private boolean hasZeroPoint;   
    private boolean hasZeroX;
    private boolean hasZeroY;

    private String currentCard = "";

    private DefaultGraphDialog(List<Graph> graphs, String title, boolean showLegend, boolean piecewise, boolean pointPlot) {
        super(TAPAALGUI.getAppGui(), title, true);
        this.graphs = graphs;
        this.showLegend = showLegend;
        this.piecewise = piecewise;
        this.pointPlot = pointPlot;
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
        ChartPanel chartPanel = createChartPanel(chart);

        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        JPanel exportPanel = new JPanel(new BorderLayout());
        JButton exportButton = new JButton("Export to TikZ");

        exportButton.addActionListener(e -> {
            if (piecewise) {
                GraphExporter.exportPiecewiseToTikz(graphs, this);
            } else if (pointPlot) {
                GraphExporter.exportPointPlotToTikz(graphs.get(0), this);
            } else {
                GraphExporter.exportToTikz(graphs.get(0), this);
            }
        });

        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(exportButton);
        
        exportPanel.add(buttonWrapper, BorderLayout.EAST);
        exportPanel.setBackground(Color.WHITE);
        
        add(exportPanel, BorderLayout.SOUTH);
    }

    private void displayWithButtons() {
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        JPanel buttonPanel = new JPanel();

        for (Graph graph : graphs) {
            JFreeChart chart = createChart(Collections.singletonList(graph));
            ChartPanel chartPanel = createChartPanel(chart);

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

        JPanel exportPanel = new JPanel(new BorderLayout());
        JButton exportButton = new JButton("Export to TikZ");
        exportButton.addActionListener(e -> {
            Graph currentGraph = graphs.stream()
                .filter(g -> g.getButtonText().equals(currentCard))
                .findFirst()
                .orElse(graphs.get(0));
            GraphExporter.exportToTikz(currentGraph, this);
        });
        
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(exportButton);
        
        exportPanel.add(buttonWrapper, BorderLayout.EAST);
        exportPanel.setBackground(Color.WHITE);

        JPanel southPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(Color.WHITE);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(exportPanel, BorderLayout.SOUTH);
        southPanel.setBackground(Color.WHITE);
    
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
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
        if (hasZeroPoint) {
            ValueAxis domainAxis = plot.getDomainAxis();
            domainAxis.setRange(negativeMargin, domainAxis.getUpperBound());
            ValueAxis rangeAxis = plot.getRangeAxis();
            rangeAxis.setRange(negativeMargin, rangeAxis.getUpperBound());
        } else if (hasZeroX) {
            ValueAxis domainAxis = plot.getDomainAxis();
            domainAxis.setRange(negativeMargin, domainAxis.getUpperBound());
        } else if (hasZeroY) {
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
        for (int i = 0; i < dataset.getSeriesCount(); ++i) {
            renderer.setSeriesStroke(i, new BasicStroke(lineThickness));
            renderer.setSeriesShapesVisible(i, pointPlot);
            renderer.setSeriesLinesVisible(i, !pointPlot);
            renderer.setSeriesPaint(i, lineColor);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        return chart;
    }

    private XYDataset constructDataset(List<Graph> graphs) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        hasZeroPoint = false;
        hasZeroX = false;
        hasZeroY = false;
        
        for (Graph graph : graphs) {
            XYSeries series = new XYSeries(graph.getName());
            List<GraphPoint> points = graph.getPoints();

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

                hasZeroX = point.getX() < margin || hasZeroX;
                hasZeroY = point.getY() < margin || hasZeroY;
            }
            
            hasZeroPoint = hasZeroX && hasZeroY;
            dataset.addSeries(series);
        }
        
        return dataset;
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
            GraphDialog dialog = new DefaultGraphDialog(graphs, title, showLegend, piecewise, pointPlot);
            return dialog;
        }

    }
}