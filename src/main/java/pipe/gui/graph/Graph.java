package pipe.gui.graph;

import java.util.List;

public class Graph {
    private final String name;
    private final List<GraphPoint> points;

    public Graph(String name, List<GraphPoint> points) {
        this.name = name;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public List<GraphPoint> getPoints() {
        return points;
    }
}
