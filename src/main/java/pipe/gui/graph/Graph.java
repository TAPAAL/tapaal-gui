package pipe.gui.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public class Graph extends AbstractGraph {
    static final int SAMPLE_COUNT = 500;

    private final List<GraphPoint> points;
    private final Double mean;
    private final DoubleUnaryOperator function;
    private final double domainStart;
    private final double domainEnd;

    public Graph(String name, List<GraphPoint> points, Double mean, String xAxisLabel, String yAxisLabel, String buttonText) {
        this(name, points, mean, xAxisLabel, yAxisLabel, buttonText, null, Double.NaN, Double.NaN);
    }

    private Graph(String name, List<GraphPoint> points, Double mean, String xAxisLabel, String yAxisLabel,
                  String buttonText, DoubleUnaryOperator function, double domainStart, double domainEnd) {
        this.name = name;
        this.points = points;
        this.mean = mean;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.buttonText = buttonText;
        this.function = function;
        this.domainStart = domainStart;
        this.domainEnd = domainEnd;
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

    public Graph(String name, double domainStart, double domainEnd, Double mean, DoubleUnaryOperator function) {
        this(name, List.of(), mean, "X axis", "Y axis", null, function, domainStart, domainEnd);
    }

    public Graph(String name, List<GraphPoint> points) {
        this(name, points, null);
    }

    public Graph(List<GraphPoint> points) {
        this(null, points);
    }

    public List<GraphPoint> getPoints() {
        return function == null ? points : sample(domainStart, domainEnd);
    }

    public Double getMean() {
        return mean;
    }

    boolean isContinuous() {
        return function != null;
    }

    List<GraphPoint> sample(double start, double end) {
        if (function == null) throw new IllegalStateException("Graph has no function to sample");
        if (start >= end) throw new IllegalArgumentException("Invalid sampling range");

        var sampled = new ArrayList<GraphPoint>(SAMPLE_COUNT);
        var step = (end - start) / (SAMPLE_COUNT - 1);
        for (var i = 0; i < SAMPLE_COUNT; ++i) {
            var x = i == SAMPLE_COUNT - 1 ? end : start + i * step;
            sampled.add(new GraphPoint(x, function.applyAsDouble(x)));
        }
        return sampled;
    }

    @Override
    public boolean isEmpty() {
        return function == null && points.isEmpty();
    }
}
