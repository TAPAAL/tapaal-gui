package dk.aau.cs.model.tapn;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public abstract class SMCDistribution {

    public abstract String distributionName();

    public abstract LinkedHashMap<String, Double> getParameters();

    public abstract String explanation();

    public Double getMean() {
        return null;
    }

    public void writeToXml(Element target) {
        target.setAttribute("distribution", distributionName());
        for(HashMap.Entry<String, Double> entry : getParameters().entrySet()) {
            target.setAttribute(entry.getKey(), entry.getValue().toString());
        }
    }

    public String toString() {
        StringBuilder res = new StringBuilder("distribution=\"" + distributionName() + "\" ");
        for(HashMap.Entry<String, Double> entry : getParameters().entrySet()) {
            res.append(entry.getKey()).append("=\"").append(entry.getValue().toString()).append("\" ");
        }
        return res.toString();
    }

    public String summary() {
        StringBuilder res = new StringBuilder(distributionName() + "(");
        LinkedList<String> params = new LinkedList<>();
        for(HashMap.Entry<String, Double> entry : getParameters().entrySet()) {
            params.add(entry.getValue().toString());
        }
        res.append(String.join(",", params));
        res.append(")");
        return res.toString();
    }

    public static SMCDistribution defaultDistribution() {
        return SMCConstantDistribution.defaultDistribution();
    }

    public static SMCDistribution defaultDistributionFor(String name) {
        switch (name) {
            case SMCConstantDistribution.NAME:
                return SMCConstantDistribution.defaultDistribution();
            case SMCUniformDistribution.NAME:
                return SMCUniformDistribution.defaultDistribution();
            case SMCExponentialDistribution.NAME:
                return SMCExponentialDistribution.defaultDistribution();
            case SMCNormalDistribution.NAME:
                return SMCNormalDistribution.defaultDistribution();
            case SMCGammaDistribution.NAME:
                return SMCGammaDistribution.defaultDistribution();
            case SMCErlangDistribution.NAME:
                return SMCErlangDistribution.defaultDistribution();
            case SMCDiscreteUniformDistribution.NAME:
                return SMCDiscreteUniformDistribution.defaultDistribution();
            case SMCGeometricDistribution.NAME:
                return SMCGeometricDistribution.defaultDistribution();
            case SMCTriangularDistribution.NAME:
                return SMCTriangularDistribution.defaultDistribution();
            case SMCLogNormalDistribution.NAME:
                return SMCLogNormalDistribution.defaultDistribution();
            default:
                return SMCDistribution.defaultDistribution();
        }
    }

    public static SMCDistribution urgent() {
        return new SMCConstantDistribution(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getParameters().equals(((SMCDistribution) o).getParameters());
    }

    public static SMCDistribution parseXml(Element elem) {
        String type = elem.getAttribute("distribution");
        try {
            switch (type) {
                case SMCConstantDistribution.NAME:
                    double value = Double.parseDouble(elem.getAttribute("value"));
                    return new SMCConstantDistribution(value);
                case SMCUniformDistribution.NAME:
                    double a = Double.parseDouble(elem.getAttribute("a"));
                    double b = Double.parseDouble(elem.getAttribute("b"));
                    return new SMCUniformDistribution(a, b);
                case SMCExponentialDistribution.NAME:
                    double rate = Double.parseDouble(elem.getAttribute("rate"));
                    return new SMCExponentialDistribution(rate);
                case SMCNormalDistribution.NAME:
                    double mean = Double.parseDouble(elem.getAttribute("mean"));
                    double stddev = Double.parseDouble(elem.getAttribute("stddev"));
                    return new SMCNormalDistribution(mean, stddev);
                case SMCGammaDistribution.NAME:
                    double shape = Double.parseDouble(elem.getAttribute("shape"));
                    double scale = Double.parseDouble(elem.getAttribute("scale"));
                    return new SMCGammaDistribution(shape, scale);
                case SMCErlangDistribution.NAME:
                    double e_shape = Double.parseDouble(elem.getAttribute("shape"));
                    double e_scale = Double.parseDouble(elem.getAttribute("scale"));
                    return new SMCErlangDistribution(e_shape, e_scale);
                case SMCDiscreteUniformDistribution.NAME:
                    double da = Double.parseDouble(elem.getAttribute("a"));
                    double db = Double.parseDouble(elem.getAttribute("b"));
                    return new SMCDiscreteUniformDistribution(da,db);
                case SMCGeometricDistribution.NAME:
                    double p = Double.parseDouble(elem.getAttribute("p"));
                    return new SMCGeometricDistribution(p);
                case SMCTriangularDistribution.NAME:
                    double t_a = Double.parseDouble(elem.getAttribute("a"));
                    double t_b = Double.parseDouble(elem.getAttribute("b"));
                    double t_c = Double.parseDouble(elem.getAttribute("c"));
                    return new SMCTriangularDistribution(t_a,t_b,t_c);
                case SMCLogNormalDistribution.NAME:
                    double logMean = Double.parseDouble(elem.getAttribute("logMean"));
                    double logStddev = Double.parseDouble(elem.getAttribute("logStddev"));
                    return new SMCLogNormalDistribution(logMean, logStddev);
            }
        } catch(NumberFormatException ignored) {}
        return SMCDistribution.defaultDistribution();
    }

}