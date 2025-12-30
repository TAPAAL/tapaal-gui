package dk.aau.cs.model.tapn;

import java.io.File;
import java.util.LinkedHashMap;
import org.w3c.dom.Element;

public class SMCUserDefinedDistribution extends SMCDistribution {

    public static final String NAME = "custom";

    private File file;

    public SMCUserDefinedDistribution(File file) {
        this.file = file;
    }

    @Override
    public String distributionName() {
        return NAME;
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
        if (file != null) {
            target.setAttribute("path", file.getAbsolutePath());
        }
    }

    @Override
    public String toString() {
        return "distribution=\"" + distributionName() + "\" path=\"" + (file != null ? file.getAbsolutePath() : "") + "\" ";
    }

    @Override
    public String summary() {
        return distributionName() + "(" + (file != null ? file.getName() : "no file") + ")";
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public static SMCUserDefinedDistribution defaultDistribution() {
        return new SMCUserDefinedDistribution(null);
    }
    
}
