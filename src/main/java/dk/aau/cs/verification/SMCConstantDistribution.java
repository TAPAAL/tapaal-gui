package dk.aau.cs.verification;

import java.util.HashMap;

public class SMCConstantDistribution extends SMCDistribution {

    public static final String NAME = "constant";

    public SMCConstantDistribution(double value) {
        this.value = value;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public HashMap<String, Double> getParameters() {
        HashMap<String, Double> params = new HashMap<>();
        params.put("value", value);
        return params;
    }

    public static SMCConstantDistribution defaultDistribution() {
        return new SMCConstantDistribution(1);
    }

    public double value;

}