package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCGeometricDistribution extends SMCDistribution {

    public static final String NAME = "geometric";

    public SMCGeometricDistribution(double p) {
        this.p = p;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("p", p);
        return params;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Geometric distribution <br/>" +
            "Model the number of trials needed to obtain on success <br/>" +
            "from successive Bernoulli trials of probability P" +
            "</html>";
    }

    @Override
    public Double getMean() {
        return 1 / p;
    }

    public static SMCGeometricDistribution defaultDistribution() {
        return new SMCGeometricDistribution(0.5);
    }

    public double p;

}
