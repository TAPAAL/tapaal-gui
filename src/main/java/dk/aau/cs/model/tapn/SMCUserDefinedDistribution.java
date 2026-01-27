package dk.aau.cs.model.tapn;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.w3c.dom.Element;

public class SMCUserDefinedDistribution extends SMCDistribution {

    public static final String NAME = "custom";

    private File file;
    private String name;
    private List<Double> values = new ArrayList<>();

    public SMCUserDefinedDistribution(File file) {
        this.file = file;
    }

    public SMCUserDefinedDistribution(String name) {
        this.name = name;
    }

    public SMCUserDefinedDistribution(String name, List<Double> values) {
        this.name = name;
        this.values = values != null ? values : new ArrayList<>();
    }

    @Override
    public String distributionName() {
        return NAME;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        target.setAttribute("distributionName", name);
    }

    @Override
    public String toString() {
        return "distribution=\"" + distributionName() + "\" distributionName=\"" + name + "\" ";
    }

    @Override
    public String summary() {
        return distributionName() + "(" + name + ")";
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public static SMCUserDefinedDistribution defaultDistribution() {
        return new SMCUserDefinedDistribution((File)null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SMCUserDefinedDistribution other = (SMCUserDefinedDistribution) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        if (values == null) {
            return other.values == null;
        }
        
        return values.equals(other.values);
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }
}
