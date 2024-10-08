package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCUniformDistribution extends SMCDistribution {

    public static final String NAME = "uniform";

    public SMCUniformDistribution(double a, double b) {
        this.a = a;
        this.b = b;
        this.mean = (a + b) / 2.0;
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
    public Double getMean() {
        return mean;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Will choose a point between two reals <br/>" + 
            "A and B, where every number has the <br/>" + 
            "same probability of being chosen." +
            "</html>";
    }

    public static SMCUniformDistribution defaultDistribution() {
        return new SMCUniformDistribution(0, 10);
    }

    public double a;
    public double b;

    private final double mean;
}