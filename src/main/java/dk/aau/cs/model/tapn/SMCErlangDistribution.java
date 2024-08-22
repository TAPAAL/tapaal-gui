package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCErlangDistribution extends SMCDistribution {

    public static final String NAME = "erlang";

    public SMCErlangDistribution(double shape, double scale) {
        this.shape = shape;
        this.scale = scale;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("shape", shape);
        params.put("scale", scale);
        return params;
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Erlang distribution <br/>" +
            "</html>";
    }

    public static SMCErlangDistribution defaultDistribution() {
        return new SMCErlangDistribution(1, 1);
    }

    public double shape;
    public double scale;

}
