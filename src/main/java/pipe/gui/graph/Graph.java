package pipe.gui.graph;

import java.util.List;

public class Graph {
    private final String name;
    private final List<GraphPoint> points;
    private final String xAxisLabel;
    private final String yAxisLabel;
    private final String buttonText;

    public Graph(String name, List<GraphPoint> points, String xAxisLabel, String yAxisLabel, String buttonText) {
        this.name = name;
        this.points = points;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.buttonText = buttonText;
    }

    public Graph(String name, List<GraphPoint> points) {
        this(name, points, "X axis", "Y axis", null);
    }

    public String getName() {
        return name;
    }

    public List<GraphPoint> getPoints() {
        return points;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public String getButtonText() {
        return buttonText;
    }
}
