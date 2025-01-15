package pipe.gui.graph;

import java.util.HashMap;
import java.util.Map;

public class MultiGraph {
    private final Map<String, Map<String, Graph>> multiGraphMap;

    private String name;
    private String xAxisLabel;
    private String yAxisLabel;
    private String buttonText;

    public MultiGraph(String name, String xAxisLabel, String yAxisLabel, String buttonText) {
        this.name = name;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.buttonText = buttonText;
        multiGraphMap = new HashMap<>(); 
    }

    public void addGraph(String observation, String property, Graph graph) {
        multiGraphMap.computeIfAbsent(observation, k -> new HashMap<>())
                     .put(property, graph);
    }

    public boolean isEmpty() {
        return multiGraphMap.values().stream().allMatch(Map::isEmpty);
    }

    public Map<String, Map<String, Graph>> getMultiGraphMap() {
        return multiGraphMap;
    }

    public String getName() {
        return name;
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
