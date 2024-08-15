package pipe.gui.graph;

import java.util.List;
import java.util.Collections;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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

    public GraphDialog(List<Graph> graphs, String frameTitle) {
        super(TAPAALGUI.getAppGui(), frameTitle, true);
        this.graphs = graphs;
    }

    public GraphDialog(List<Graph> graphs, String frameTitle, boolean showLegend) {
        this(graphs, frameTitle);
        this.showLegend = showLegend;
    }

    public GraphDialog(List<Graph> graphs, String frameTitle, boolean showLegend, boolean piecewise) {
        this(graphs, frameTitle, showLegend);
        this.piecewise = piecewise;
    }

    public GraphDialog(List<Graph> graphs, String frameTitle, boolean showLegend, boolean piecewise, boolean pointPlot) {
        this(graphs, frameTitle, showLegend, piecewise);
        this.pointPlot = pointPlot;
    }

    public GraphDialog(Graph graph, String frameTitle, boolean showLegend, boolean piecewise, boolean pointPlot) {
        this(List.of(graph), frameTitle, showLegend, piecewise, pointPlot);
    }

    public GraphDialog(Graph graph, String frameTitle) {
        this(List.of(graph), frameTitle);
    }

    public GraphDialog(Graph graph, String frameTitle, boolean showLegend) {
        this(List.of(graph), frameTitle, showLegend);
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
        JFreeChart chart = ChartFactory.createXYLineChart(graphs.get(0).getName(), graphs.get(0).getXAxisLabel(), graphs.get(0).getYAxisLabel(), dataset, PlotOrientation.VERTICAL, showLegend, true, false);

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        Color lineColor = Color.RED;
        float lineThickness = 3.0f;
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
            for (GraphPoint point : graph.getPoints()) {
                series.add(point.getX(), point.getY());
            }
            dataset.addSeries(series);
        }
        return dataset;
    }
}