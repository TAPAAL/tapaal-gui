package pipe.gui.graph;

public abstract class AbstractGraph {
    protected String name;
    protected String xAxisLabel;
    protected String yAxisLabel;
    protected String buttonText;

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

    public abstract boolean isEmpty();
}
