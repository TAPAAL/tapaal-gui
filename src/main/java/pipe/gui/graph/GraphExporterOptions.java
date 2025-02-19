package pipe.gui.graph;

public class GraphExporterOptions {
    private boolean showLegend;
    private boolean standalone;
    private boolean piecewise;
    private boolean pointPlot;
    private Resolution resolution = Resolution.HIGH;

    public enum Resolution {
        LOW(4), MEDIUM(2), HIGH(1);
        
        private final int step;
        
        Resolution(int step) {
            this.step = step;
        }
        
        public int getStep() {
            return step;
        }
    }

    public boolean showLegend() {
        return showLegend;
    }

    public boolean isStandalone() {
        return standalone;
    }

    public boolean isPiecewise() {
        return piecewise;
    }

    public boolean isPointPlot() {
        return pointPlot;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public void setPiecewise(boolean piecewise) {
        this.piecewise = piecewise;
    }

    public void setPointPlot(boolean pointPlot) {
        this.pointPlot = pointPlot;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }
}   
