package dk.aau.cs.verification;

import java.util.HashMap;

public class SMCExponentialDistribution extends SMCDistribution {

    public static final String NAME = "exponential";

    public SMCExponentialDistribution(double rate) {
        this.rate = rate;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public HashMap<String, Double> getParameters() {
        HashMap<String, Double> params = new HashMap<>();
        params.put("rate", rate);
        return params;
    }

    public static SMCExponentialDistribution defaultDistribution() {
        return new SMCExponentialDistribution(0.1);
    }

    public double rate;

}