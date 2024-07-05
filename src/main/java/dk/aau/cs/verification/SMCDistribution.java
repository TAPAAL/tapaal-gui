package dk.aau.cs.verification;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

public abstract class SMCDistribution {

    public abstract String distributionName();

    public abstract LinkedHashMap<String, Double> getParameters();

    public abstract String explanation();

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
            }
        } catch(NumberFormatException ignored) {}
        return SMCDistribution.defaultDistribution();
    }

}