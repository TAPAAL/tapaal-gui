package dk.aau.cs.model.tapn;

import java.util.LinkedHashMap;

public class SMCLogNormalDistribution extends SMCDistribution {

    public static final String NAME = "log normal";

    public SMCLogNormalDistribution(double mu, double sigma) {
        logMean = mu;
        logStddev = sigma;
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        params.put("logMean", logMean);
        params.put("logStddev", logStddev);
        return params;
    }

    @Override
    public Double getMean() {
        return Math.exp(logMean + (Math.pow(logStddev, 2) / 2));
    }

    @Override
    public String explanation() {
        return "<html>" +
            "Distribution which Log is normally distributed, <br/>" +
            "meaning if X follows a log-normal distribution, <br/>" +
            "ln(X) follows a normal distribution. <br/>" +
            "The distribution is parameterized by the mean and the std. dev. of its logarithm." +
            "</html>";
    }

    public static SMCLogNormalDistribution defaultDistribution() {
        return new SMCLogNormalDistribution(0, 1);
    }

    public double pdf(double x) {
        double exp_num = - Math.pow(Math.log(x) - logMean, 2);
        double exp_den = 2 * Math.pow(logStddev, 2);
        double exp = Math.exp(exp_num / exp_den);
        double fact = 1 / (x * logStddev * Math.sqrt(2 * Math.PI));
        return fact * exp;
    }

    public double logMean, logStddev;


}
