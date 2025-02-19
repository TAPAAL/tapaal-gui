package pipe.gui.graph;

import java.util.List;

public class Graph extends AbstractGraph {
    private final List<GraphPoint> points;
    private final Double mean;

    public Graph(String name, List<GraphPoint> points, Double mean, String xAxisLabel, String yAxisLabel, String buttonText) {
        this.name = name;
        this.points = points;
        this.mean = mean;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.buttonText = buttonText;
    }

    public Graph(String name, List<GraphPoint> points, String xAxisLabel, String yAxisLabel, String buttonText) {
        this(name, points, null, xAxisLabel, yAxisLabel, buttonText);
    }

    public Graph(String name, List<GraphPoint> points, Double mean, String xAxisLabel, String yAxisLabel) {
        this(name, points, mean, xAxisLabel, yAxisLabel, null);
    }

    public Graph(String name, List<GraphPoint> points, Double mean) {
        this(name, points, mean, "X axis", "Y axis", null);
    }

    public Graph(String name, List<GraphPoint> points) {
        this(name, points, null);
    }

    public Graph(List<GraphPoint> points) {
        this(null, points);
    }

    public List<GraphPoint> getPoints() {
        return points;
    }

    public Double getMean() {
        return mean;
    }

    @Override
    public boolean isEmpty() {
        return points.isEmpty();
    }
}
