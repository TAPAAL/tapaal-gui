package dk.aau.cs.verification;

import java.util.HashMap;

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
    public HashMap<String, Double> getParameters() {
        HashMap<String, Double> params = new HashMap<>();
        params.put("mean", mean);
        params.put("stddev", stddev);
        return params;
    }

    public static SMCNormalDistribution defaultDistribution() {
        return new SMCNormalDistribution(1, 1);
    }

    public double mean;
    public double stddev;

}
