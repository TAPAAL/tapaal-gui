package dk.aau.cs.verification;

import java.util.HashMap;
import java.util.LinkedHashMap;

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
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("value", value);
        return params;
    }

    @Override
    public String explanation() {
        return "Will always choose the same specified value.";
    }

    public static SMCConstantDistribution defaultDistribution() {
        return new SMCConstantDistribution(1);
    }

    public double value;

}