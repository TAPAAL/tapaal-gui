package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCUniformDistribution extends SMCDistribution {

    public static final String NAME = "uniform";

    public SMCUniformDistribution(double a, double b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("a", a);
        params.put("b", b);
        return params;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Will choose a point between two reals A and B, <br/>" +
            "where every number the same probability of being chosen.<br/>" +
            "Mean : " + ((this.b - this.a) / 2.0) +
            "</html>";
    }

    public static SMCUniformDistribution defaultDistribution() {
        return new SMCUniformDistribution(0, 10);
    }

    public double a;
    public double b;

}