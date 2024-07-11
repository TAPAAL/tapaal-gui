package dk.aau.cs.verification;

import java.util.HashMap;
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

    @Override
    public String explanation() {
        return "Gaussian distribution, centered around a real MEAN, and spread according to a standard deviation STDDEV.";
    }

    public static SMCNormalDistribution defaultDistribution() {
        return new SMCNormalDistribution(1, 1);
    }

    public double mean;
    public double stddev;

}
