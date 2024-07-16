package dk.aau.cs.verification;

import java.util.HashMap;
import java.util.LinkedHashMap;

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
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("rate", rate);
        return params;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Memoryless distribution, <br/>" +
            "probability of the distance between events occurring according to a real rate.<br/>" +
            "Mean : " + (1.0 / this.rate) +
            "</html>";
    }

    public static SMCExponentialDistribution defaultDistribution() {
        return new SMCExponentialDistribution(0.1);
    }

    public double rate;

}