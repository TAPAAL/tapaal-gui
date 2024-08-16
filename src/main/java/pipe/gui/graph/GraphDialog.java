package pipe.gui.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Shape;
import java.awt.geom.Line2D;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import pipe.gui.TAPAALGUI;
import pipe.gui.swingcomponents.EscapableDialog;

public class GraphDialog extends EscapableDialog {
    private final List<Graph> graphs;
    
    private boolean showLegend;
    private boolean piecewise;
    private boolean pointPlot;
    private boolean isStraight;
    private double distanceToOrigin;
    private Double mean;

    private GraphDialog(List<Graph> graphs, String title, boolean showLegend, boolean piecewise, boolean pointPlot) {
        super(TAPAALGUI.getAppGui(), title, true);
        this.graphs = graphs;
        this.showLegend = showLegend;
        this.piecewise = piecewise;
        this.pointPlot = pointPlot;
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
            GraphDialog dialog = new GraphDialog(graphs, title, showLegend, piecewise, pointPlot);
            return dialog;
        }

    }

    public void display() {
        if (piecewise || graphs.size() < 2) {
            displayWithoutButtons();
        } else {
            displayWithButtons();
        }
    }

    private void displayWithoutButtons() {
        JFreeChart chart = createChart(graphs);
        ChartPanel chartPanel = createChartPanel(chart);

        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        setupDialog();
    }

    private void displayWithButtons() {
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        JPanel buttonPanel = new JPanel();

        for (Graph graph : graphs) {
            JFreeChart chart = createChart(Collections.singletonList(graph));
            ChartPanel chartPanel = createChartPanel(chart);

            String buttonText = graph.getButtonText();
            if (buttonText != null) {
                cardPanel.add(chartPanel, buttonText);
                addButton(buttonPanel, cardLayout, cardPanel, buttonText);
            } else {
                cardPanel.add(chartPanel, graph.getName());
                addButton(buttonPanel, cardLayout, cardPanel, graph.getName());
            }
        }

        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setupDialog();
    }

    private void setupDialog() {
        setSize(800, 600);
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private ChartPanel createChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        return chartPanel;
    }

    private void addButton(JPanel buttonPanel, CardLayout cardLayout, JPanel cardPanel, String buttonText) {
        JButton button = new JButton(buttonText);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, buttonText);
            }
        });

        buttonPanel.add(button);
        buttonPanel.setBackground(Color.WHITE);
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

            XYLineAnnotation annotation = new XYLineAnnotation(mean, 0, mean, rangeAxis.getUpperBound(), dashed, Color.BLACK);
            plot.addAnnotation(annotation);

            Shape lineShape = new Line2D.Double(0, 0, 30, 0);
            LegendItemCollection legendItems;
            
            legendItems = showLegend ? plot.getLegendItems() : new LegendItemCollection();
            legendItems.add(new LegendItem("Mean", null, null, null, lineShape, Color.BLACK, dashed, Color.BLACK));
            plot.setFixedLegendItems(legendItems);
        }

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
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
        for (Graph graph : graphs) {
            XYSeries series = new XYSeries(graph.getName());
            List<GraphPoint> points = graph.getPoints();

            if (!points.isEmpty()) {
                double first = points.get(0).getX();
                double last = points.get(points.size() - 1).getX();

                isStraight = (first - last == 0) && !piecewise;
                distanceToOrigin = first;
            }

            if (graph.getMean() != null) {
                mean = graph.getMean();
            }

            for (GraphPoint point : points) {
                series.add(point.getX(), point.getY());
            }
            
            dataset.addSeries(series);
        }
        return dataset;
    }
}