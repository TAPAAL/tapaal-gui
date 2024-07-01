package dk.aau.cs.verification;

import java.util.HashMap;

public class SMCUniformDistribution extends SMCDistribution {

    public static final String NAME = "uniform";

    public SMCUniformDistribution(double a, double b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public HashMap<String, Double> getParameters() {
        HashMap<String, Double> params = new HashMap<>();
        params.put("a", a);
        params.put("b", b);
        return params;
    }

    public static SMCUniformDistribution defaultDistribution() {
        return new SMCUniformDistribution(0, 10);
    }

    public double a;
    public double b;

}