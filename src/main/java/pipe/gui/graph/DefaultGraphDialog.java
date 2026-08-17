package pipe.gui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYLineAnnotation;
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
    private static final String BIN_WIDTH_PLACEHOLDER = "Bin Width: 1";

    private final List<Graph> graphs;
    private final boolean showLegend;
    private final boolean piecewise;
    private final boolean pointPlot;
    
    private boolean isStraight;
    private double distanceToOrigin;
    private Double mean;
    private double binWidth = -1;

    private boolean hasZeroX;
    private boolean hasZeroY;

    private String currentCard = "";
    private final List<DraggableChartPanel> chartPanels = new ArrayList<>();
    private DraggableChartPanel singleChartPanel;

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

        setSize(800, 600);
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private void displayWithoutButtons() {
        var chart = createChart(graphs);
        singleChartPanel = new DraggableChartPanel(chart);

        setLayout(new BorderLayout());
        add(singleChartPanel, BorderLayout.CENTER);

        var exportPanel = createExportPanel(e -> {
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
        var cardLayout = new CardLayout();
        var cardPanel = new JPanel(cardLayout);
        var buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        for (var graph : graphs) {
            var chart = createChart(Collections.singletonList(graph));
            var chartPanel = new DraggableChartPanel(chart);
            chartPanels.add(chartPanel);

            var buttonText = graph.getButtonText();
            cardPanel.add(chartPanel, buttonText);

            var button = new JButton(buttonText);
            button.addActionListener(e -> {
                cardLayout.show(cardPanel, buttonText);
                currentCard = buttonText;
            });
            buttonPanel.add(button);
        }

        currentCard = graphs.get(0).getButtonText();

        var exportPanel = createExportPanel(e -> {
            var currentGraph = graphs.stream()
                .filter(g -> g.getButtonText().equals(currentCard))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No graph found for current card: " + currentCard));
            GraphExporter.exportToTikz(currentGraph, DefaultGraphDialog.this);
        });

        var southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(Color.WHITE);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(exportPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createExportPanel(ActionListener exportAction) {
        var exportPanel = new JPanel(new BorderLayout());
        exportPanel.setBackground(Color.WHITE);

        var exportButton = new JButton("Export to TikZ");
        exportButton.addActionListener(exportAction);
        var resetButton = new JButton("Reset View");
        resetButton.addActionListener(e -> resetView());
        
        var buttonWrapper = new JPanel();
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(resetButton);
        buttonWrapper.add(exportButton);

        if (pointPlot) {
            var sliderPanel = new JPanel();
            sliderPanel.setBackground(Color.WHITE);
            var slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
            slider.setBackground(Color.WHITE);
            var label = new JLabel(BIN_WIDTH_PLACEHOLDER);
            label.setPreferredSize(new Dimension(125, 20));

            slider.addChangeListener(e -> {
                var value = ((JSlider)e.getSource()).getValue();
                if (value == 0) {
                    binWidth = -1;
                    label.setText(BIN_WIDTH_PLACEHOLDER);
                } else {
                    var stats = graphs.stream()
                                                          .flatMap(g -> g.getPoints().stream())
                                                          .mapToDouble(GraphPoint::getX)
                                                          .summaryStatistics();

                    var range = stats.getMax() - stats.getMin();
                    var natural = naturalResolution(graphs);
                    var maxMultiple = (int)Math.round(range / natural) + 1;

                    var validMultiples = new ArrayList<Integer>();
                    for (var i = 1; i <= maxMultiple; ++i) {
                        if ((i * natural) >= 1.0) {
                            validMultiples.add(i);
                        }
                    }

                    if (validMultiples.isEmpty()) {
                        binWidth = -1;
                        label.setText(BIN_WIDTH_PLACEHOLDER);
                    } else {
                        var idx = (int)Math.round(((value - 1) / 99.0) * (validMultiples.size() - 1));
                        idx = Math.max(0, Math.min(idx, validMultiples.size() - 1));
                        binWidth = validMultiples.get(idx) * natural;
                        label.setText(String.format("Bin Width: %d", Math.round(binWidth)));
                    }
                }
                updateDataset();
            });

            sliderPanel.add(label);
            sliderPanel.add(slider);
            exportPanel.add(sliderPanel, BorderLayout.WEST);
        }

        exportPanel.add(buttonWrapper, BorderLayout.EAST);
        return exportPanel;
    }

    private void resetView() {
        if (singleChartPanel != null) {
            singleChartPanel.resetView();
            return;
        }

        chartPanels.stream()
            .filter(DraggableChartPanel::isVisible)
            .findFirst()
            .ifPresent(DraggableChartPanel::resetView);
    }

    private double getResolution(double[] xs) {
        var minGap = Double.MAX_VALUE;
        for (var i = 1; i < xs.length; ++i) {
            minGap = Math.min(minGap, xs[i] - xs[i - 1]);
        }
        return (minGap == Double.MAX_VALUE || minGap <= 0) ? 1.0 : minGap;
    }

    private double naturalResolution(List<Graph> graphs) {
        return getResolution(graphs.stream()
            .flatMap(g -> g.getPoints().stream())
            .mapToDouble(GraphPoint::getX)
            .distinct()
            .sorted()
            .toArray());
    }

    private boolean isBinningEnabled(List<Graph> graphList) {
        return binWidth > 0 && graphList.stream().anyMatch(g -> g.getPoints().size() > 1);
    }

    private JFreeChart createChart(List<Graph> graphs) {
        var dataset = constructDataset(graphs);
        JFreeChart chart;
        
        if (graphs.isEmpty()) {
            chart = ChartFactory.createXYLineChart("", "X", "Y", dataset, PlotOrientation.VERTICAL, false, true, false);
        } else {
            chart = ChartFactory.createXYLineChart(
                graphs.get(0).getName(),
                graphs.get(0).getXAxisLabel(),
                graphs.get(0).getYAxisLabel(),
                dataset,
                PlotOrientation.VERTICAL,
                showLegend || mean != null,
                true,
                false
            );
        }

        var plot = chart.getXYPlot();
        var lineThickness = 3.0f;
        final var negativeMargin = -0.01;

        if (hasZeroX) {
            var domainAxis = plot.getDomainAxis();
            domainAxis.setRange(negativeMargin, domainAxis.getUpperBound());
        }

        if (hasZeroY) {
            var rangeAxis = plot.getRangeAxis();
            rangeAxis.setRange(negativeMargin, rangeAxis.getUpperBound());
        }

        if (isStraight) {
            var domainAxis = plot.getDomainAxis();
            domainAxis.setRange(distanceToOrigin - 1, distanceToOrigin + 1);
        } else if (mean != null) {
            var rangeAxis = plot.getRangeAxis();
            var dashed = new BasicStroke(
                lineThickness,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                new float[]{ 5.0f, 5.0f },
                0.0f
            );

            var annotation = new XYLineAnnotation(
                mean, rangeAxis.getLowerBound(), mean, rangeAxis.getUpperBound(), dashed, Color.BLACK
            );
            plot.addAnnotation(annotation);

            var lineShape = new Line2D.Double(0, 0, 30, 0);
            var legendItems = showLegend ? plot.getLegendItems() : new LegendItemCollection();
            legendItems.add(new LegendItem("Mean", null, null, null, lineShape, Color.BLACK, dashed, Color.BLACK));
            plot.setFixedLegendItems(legendItems);
        }

        var renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        var usesBinning = isBinningEnabled(graphs);

        for (var i = 0; i < dataset.getSeriesCount(); ++i) {
            renderer.setSeriesStroke(i, new BasicStroke(lineThickness));
            renderer.setSeriesShapesVisible(i, pointPlot);
            renderer.setSeriesLinesVisible(i, !pointPlot || usesBinning);
            renderer.setSeriesPaint(i, Color.RED);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);

        return chart;
    }

    private void updateDataset() {
        if (singleChartPanel != null) {
            updatePlotDataset(singleChartPanel.getChart().getXYPlot(), constructDataset(graphs), isBinningEnabled(graphs));
        } else {
            for (var i = 0; i < Math.min(graphs.size(), chartPanels.size()); ++i) {
                var singleGraphList = Collections.singletonList(graphs.get(i));
                updatePlotDataset(
                    chartPanels.get(i).getChart().getXYPlot(),
                    constructDataset(singleGraphList),
                    isBinningEnabled(singleGraphList)
                );
            }
        }
    }

    private void updatePlotDataset(XYPlot plot, XYDataset dataset, boolean usesBinning) {
        plot.setDataset(dataset);
        var renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        for (var i = 0; i < dataset.getSeriesCount(); ++i) {
            renderer.setSeriesLinesVisible(i, !pointPlot || usesBinning);
        }
    }

    private XYDataset constructDataset(List<Graph> graphs) {
        var dataset = new XYSeriesCollection();
        hasZeroX = false;
        hasZeroY = false;

        for (var graph : graphs) {
            var series = new XYSeries(graph.getName());
            var points = graph.getPoints();

            if (binWidth > 0 && points.size() > 1) {
                points = binPoints(points, binWidth);
            }

            var margin = 1e-5;
            if (!points.isEmpty()) {
                var first = points.get(0).getX();
                var last = points.get(points.size() - 1).getX();
                isStraight = Math.abs(first - last) < margin && !piecewise;
                distanceToOrigin = first;
            }

            if (graph.getMean() != null) {
                mean = graph.getMean();
            }

            for (var point : points) {
                Require.that(point.getX() >= 0 && point.getY() >= 0, "Negative points are not supported");
                series.add(point.getX(), point.getY());
                hasZeroX |= point.getX() < margin;
                hasZeroY |= point.getY() < margin;
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    private List<GraphPoint> binPoints(List<GraphPoint> points, double binWidth) {
        if (points == null || points.isEmpty()) return Collections.emptyList();

        var stats = points.stream().mapToDouble(GraphPoint::getX).summaryStatistics();
        var minX = stats.getMin();
        var maxX = stats.getMax();

        if (minX == maxX) return Collections.emptyList();

        var natural = getResolution(points.stream().mapToDouble(GraphPoint::getX).distinct().sorted().toArray());
        var start = minX - (natural / 2.0);
        var end = maxX + (natural / 2.0);

        var binCount = (int)Math.ceil((end - start) / binWidth);

        var binSums = new double[binCount];
        for (var p : points) {
            binSums[(int)((p.getX() - start) / binWidth)] += p.getY();
        }

        var binned = new ArrayList<GraphPoint>();
        for (var i = 0; i < binCount; ++i) {
            if (binSums[i] > 0) {
                var binStart = start + i * binWidth;
                var binEnd = Math.min(start + (i + 1) * binWidth, end);
                var actualBinWidth = binEnd - binStart;
                
                binned.add(new GraphPoint(binStart + actualBinWidth / 2.0, binSums[i] / actualBinWidth));
            }
        }
        return binned;
    }

    public static class GraphDialogBuilder {
        private final List<Graph> graphs = new ArrayList<>();
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
