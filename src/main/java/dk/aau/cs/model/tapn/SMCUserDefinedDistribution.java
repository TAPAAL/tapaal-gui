package dk.aau.cs.model.tapn;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.w3c.dom.Element;

public class SMCUserDefinedDistribution extends SMCDistribution {

    public static final String NAME = "custom";

    private File file;
    private String customDistributionName;
    private List<Double> values = new ArrayList<>();

    public SMCUserDefinedDistribution(File file) {
        this.file = file;
    }

    public SMCUserDefinedDistribution(String customDistributionName) {
        this.customDistributionName = customDistributionName;
    }

    public SMCUserDefinedDistribution(String name, List<Double> values) {
        this.customDistributionName = name;
        this.values = values != null ? values : new ArrayList<>();
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    public String getName() {
        return customDistributionName;
    }

    public List<Double> getValues() {
        return values;
    }

    @Override
    public LinkedHashMap<String, Double> getParameters() {
        return new LinkedHashMap<>();
    }

    @Override
    public String explanation() {
        return "<html>Samples values from a user-defined file.<br>Each line in the file should contain a single sampled value.</html>";
    }

    @Override
    public void writeToXml(Element target) {
        target.setAttribute("distribution", distributionName());
        target.setAttribute("distributionName", customDistributionName);
    }

    @Override
    public String toString() {
        return "distribution=\"" + distributionName() + "\" distributionName=\"" + customDistributionName + "\" ";
    }

    @Override
    public String summary() {
        return distributionName() + "(" + customDistributionName + ")";
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getCustomDistributionName() {
        return customDistributionName;
    }

    public static SMCUserDefinedDistribution defaultDistribution() {
        return new SMCUserDefinedDistribution((File)null);
    }
    
}
