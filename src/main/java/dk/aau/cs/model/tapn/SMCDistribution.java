package dk.aau.cs.model.tapn;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class SMCDistribution {
    private final LinkedHashMap<String, SMCParameterConstant> paramRefs = new LinkedHashMap<>();

    public abstract String distributionName();

    public abstract LinkedHashMap<String, Double> getParameters();

    public abstract String explanation();

    public Map<String, SMCParameterConstant> getParamRefs() {
        return paramRefs;
    }

    public void setParamRef(String param, SMCParameterConstant constant) {
        if (constant == null) {
            paramRefs.remove(param);
            return;
        } 

        paramRefs.put(param, constant);
    }

    public SMCParameterConstant getParamRef(String param) {
        return paramRefs.get(param);
    }

    public Map<String, Double> getResolvedParameters() {
        var params = getParameters();
        for (var entry : paramRefs.entrySet()) {
            if (params.containsKey(entry.getKey())) {
                params.put(entry.getKey(), entry.getValue().paramValue());
            }
        }

        return params;
    }

    public Double getMean() {
        return null;
    }

    public void writeToXml(Element target) {
        writeToXml(target, true);
    }

    public void writeToXml(Element target, boolean writeConstantNames) {
        target.setAttribute("distribution", distributionName());
        var params = writeConstantNames ? getParameters() : getResolvedParameters();
        for (var entry : params.entrySet()) {
            var ref = paramRefs.get(entry.getKey());
            if (writeConstantNames && ref != null) {
                target.setAttribute(entry.getKey(), ref.name());
                continue;
            }

            target.setAttribute(entry.getKey(), entry.getValue().toString());
        }
    }

    public String toString() {
        StringBuilder res = new StringBuilder("distribution=\"" + distributionName() + "\" ");
        for (var entry : getResolvedParameters().entrySet()) {
            res.append(entry.getKey()).append("=\"").append(entry.getValue().toString()).append("\" ");
        }
        return res.toString();
    }

    public String summary() {
        StringBuilder res = new StringBuilder(distributionName() + "(");
        LinkedList<String> params = new LinkedList<>();
        for (var entry : getParameters().entrySet()) {
            var ref = paramRefs.get(entry.getKey());
            params.add(ref != null ? ref.name() : entry.getValue().toString());
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
            case SMCUserDefinedDistribution.NAME:
                return SMCUserDefinedDistribution.defaultDistribution();
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
        return parseXml(elem, null);
    }

    public static SMCDistribution parseXml(Element elem, ConstantStore constants) {
        String type = elem.getAttribute("distribution");
        var refs = new LinkedHashMap<String, SMCParameterConstant>();
        try {
            SMCDistribution result = null;
            switch (type) {
                case SMCConstantDistribution.NAME:
                    double value = parseParam(elem, "value", constants, refs);
                    result = new SMCConstantDistribution(value);
                    break;
                case SMCUniformDistribution.NAME:
                    double a = parseParam(elem, "a", constants, refs);
                    double b = parseParam(elem, "b", constants, refs);
                    result = new SMCUniformDistribution(a, b);
                    break;
                case SMCExponentialDistribution.NAME:
                    double rate = parseParam(elem, "rate", constants, refs);
                    result = new SMCExponentialDistribution(rate);
                    break;
                case SMCNormalDistribution.NAME:
                    double mean = parseParam(elem, "mean", constants, refs);
                    double stddev = parseParam(elem, "stddev", constants, refs);
                    result = new SMCNormalDistribution(mean, stddev);
                    break;
                case SMCGammaDistribution.NAME:
                    double shape = parseParam(elem, "shape", constants, refs);
                    double scale = parseParam(elem, "scale", constants, refs);
                    result = new SMCGammaDistribution(shape, scale);
                    break;
                case SMCErlangDistribution.NAME:
                    double e_shape = parseParam(elem, "shape", constants, refs);
                    double e_scale = parseParam(elem, "scale", constants, refs);
                    result = new SMCErlangDistribution(e_shape, e_scale);
                    break;
                case SMCDiscreteUniformDistribution.NAME:
                    double da = parseParam(elem, "a", constants, refs);
                    double db = parseParam(elem, "b", constants, refs);
                    result = new SMCDiscreteUniformDistribution(da,db);
                    break;
                case SMCGeometricDistribution.NAME:
                    double p = parseParam(elem, "p", constants, refs);
                    result = new SMCGeometricDistribution(p);
                    break;
                case SMCTriangularDistribution.NAME:
                    double t_a = parseParam(elem, "a", constants, refs);
                    double t_b = parseParam(elem, "b", constants, refs);
                    double t_c = parseParam(elem, "c", constants, refs);
                    result = new SMCTriangularDistribution(t_a,t_b,t_c);
                    break;
                case SMCLogNormalDistribution.NAME:
                    double logMean = parseParam(elem, "logMean", constants, refs);
                    double logStddev = parseParam(elem, "logStddev", constants, refs);
                    result = new SMCLogNormalDistribution(logMean, logStddev);
                    break;
                case SMCUserDefinedDistribution.NAME:
                    String distributionName = elem.getAttribute("distributionName");
                    result = new SMCUserDefinedDistribution(distributionName);
                    break;
            }
            if (result != null) {
                refs.forEach(result::setParamRef);
                return result;
            }
        } catch(NumberFormatException ignored) {}
        return SMCDistribution.defaultDistribution();
    }

    private static double parseParam(Element elem, String attr, ConstantStore constants, Map<String, SMCParameterConstant> refs) {
        String raw = elem.getAttribute(attr);
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            if (constants != null) {
                SMCParameterConstant c = constants.getRealConstantByName(raw);
                if (c == null) {
                    c = constants.getConstantByName(raw);
                }
                
                if (c != null) {
                    refs.put(attr, c);
                    return c.paramValue();
                }
            }

            throw e;
        }
    }

}