package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCNormalDistribution extends SMCDistribution {

    public static final String NAME = "normal";

    public SMCNormalDistribution(double mean, double stddev) {
        this.mean = mean;
        this.stddev = stddev;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("mean", mean);
        params.put("stddev", stddev);
        return params;
    }

    public double getMean() {
        return mean;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Gaussian distribution, centered around <br/>" + 
            "a real MEAN, and spread according to <br/>" + 
            "a standard deviation STDDEV." +
            "</html>";
    }

    public static SMCNormalDistribution defaultDistribution() {
        return new SMCNormalDistribution(1, 1);
    }

    public double mean;
    public double stddev;

}
