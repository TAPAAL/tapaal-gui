package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCTriangularDistribution extends SMCDistribution {

    public static final String NAME = "triangular";

    public SMCTriangularDistribution(double x, double y, double z) {
        a = x;
        b = y;
        c = z;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("a", a);
        params.put("b", b);
        params.put("c", c);
        return params;
    }

    @Override
    public Double getMean() {
        return (a + b + c) / 3;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Distribution between two reals A and B with A ≤ C ≤ B, <br/>" +
            "in which the density function increases linearly from A to C <br/>" +
            "and then decreases from C to B" +
            "</html>";
    }

    public static SMCTriangularDistribution defaultDistribution() {
        return new SMCTriangularDistribution(0, 1, 0.5);
    }

    public double a,b,c;

}
