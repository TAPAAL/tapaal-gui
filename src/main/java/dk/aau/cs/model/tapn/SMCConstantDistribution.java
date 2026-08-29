package dk.aau.cs.model.tapn;

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
        return
            "<html>" +
            "Will always choose the same specified value." +
            "</html>";
    }

    public static SMCConstantDistribution defaultDistribution() {
        return new SMCConstantDistribution(1);
    }

    public double value;

}