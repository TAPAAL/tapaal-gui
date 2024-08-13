package pipe.gui.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JDialog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import net.tapaal.TAPAAL;
import pipe.gui.TAPAALGUI;

import org.jfree.data.xy.XYSeries;

public class GraphFrame extends JDialog {
    private final String title;
    private final List<Graph> graphs;

    public GraphFrame(String frameTitle, List<Graph> graphs, String title) {
        super(TAPAALGUI.getAppGui(), frameTitle, true);
        this.graphs = graphs;
        this.title = title;
    }

    public void draw() {
        XYDataset dataset = constructDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(title, "X axis", "Y axis", dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
        setSize(800, 600);
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }    

    private XYDataset constructDataset() {        
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
