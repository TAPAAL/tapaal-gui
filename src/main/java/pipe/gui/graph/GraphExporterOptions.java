package pipe.gui.graph;

import java.awt.Color;
import java.util.Map;

public class GraphExporterOptions {
    private boolean showLegend;
    private boolean standalone;
    private boolean piecewise;
    private boolean pointPlot;
    private boolean isMultiGraph;
    private double heightMultiplier = 1;
    private double widthMultiplier = 1;

    private Resolution resolution = Resolution.HIGH;
    private LegendPosition legendPosition;

    private Map<String, Color> colorMappings;

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

    public enum LegendPosition {
        NW("North West", "(0.02,0.98)"),
        NE("North East", "(0.98,0.98)"),
        SW("South West", "(0.02,0.02)"),
        SE("South East", "(0.98,0.02)");
    
        private final String display;
        private final String coordinates;
    
        LegendPosition(String display, String coordinates) {
            this.display = display;
            this.coordinates = coordinates;
        }
    
        public String getCoordinates() { 
            return coordinates; 
        }
    
        @Override
        public String toString() {
            return display;
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

    public boolean isMultiGraph() {
        return isMultiGraph;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public LegendPosition getLegendPosition() {
        return legendPosition;
    }

    public Map<String, Color> getColorMappings() {
        return colorMappings;
    }

    public double getHeightMultiplier() {
        return heightMultiplier;
    }

    public double getWidthMultiplier() {
        return widthMultiplier;
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

    public void setMultiGraph(boolean isMultiGraph) {
        this.isMultiGraph = isMultiGraph;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public void setLegendPosition(LegendPosition legendPosition) {
        this.legendPosition = legendPosition;
    }

    public void setColorMappings(Map<String, Color> colorMappings) {
        this.colorMappings = colorMappings;
    }

    public void setHeightMultiplier(double heightMultiplier) {
        this.heightMultiplier = heightMultiplier;
    }

    public void setWidthMultiplier(double widthMultiplier) {
        this.widthMultiplier = widthMultiplier;
    }
}   
