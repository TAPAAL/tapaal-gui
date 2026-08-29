package dk.aau.cs.verification.VerifyTAPN;

import java.util.ArrayList;
import java.util.List;

import pipe.gui.graph.GraphPoint;

public class ObservationData {
    private List<GraphPoint> smcObservationAvgStep = new ArrayList<>();
    private List<GraphPoint> smcObservationMinStep = new ArrayList<>();
    private List<GraphPoint> smcObservationMaxStep = new ArrayList<>();
    private List<GraphPoint> smcObservationAvgTime = new ArrayList<>();
    private List<GraphPoint> smcObservationMinTime = new ArrayList<>();
    private List<GraphPoint> smcObservationMaxTime = new ArrayList<>();
    private List<GraphPoint> smcObservationValueStep = new ArrayList<>();
    private List<GraphPoint> smcObservationValueTime = new ArrayList<>();

    private double smcGlobalAvgStep;
    private double smcGlobalAvgTime;

    public void setObservationData(List<GraphPoint> points, String key) {
        switch (key) {
            case "avgStep":
                setSmcObservationAvgStep(points);
                break;
            case "minStep":
                setSmcObservationMinStep(points);
                break;
            case "maxStep":
                setSmcObservationMaxStep(points);
                break;
            case "avgTime":
                setSmcObservationAvgTime(points);
                break;
            case "minTime":
                setSmcObservationMinTime(points);
                break;
            case "maxTime":
                setSmcObservationMaxTime(points);
                break;
            case "valueStep":
                setSmcObservationValueStep(points);
                break;
            case "valueTime":
                setSmcObservationValueTime(points);
                break;
            default:
                throw new IllegalArgumentException("Invalid key: " + key);
        }
    }

    private void setSmcObservationAvgStep(List<GraphPoint> smcObservationAvgStep) {
        this.smcObservationAvgStep = smcObservationAvgStep;
    }

    private void setSmcObservationMinStep(List<GraphPoint> smcObservationMinStep) {
        this.smcObservationMinStep = smcObservationMinStep;
    }

    private void setSmcObservationMaxStep(List<GraphPoint> smcObservationMaxStep) {
        this.smcObservationMaxStep = smcObservationMaxStep;
    }

    private void setSmcObservationAvgTime(List<GraphPoint> smcObservationAvgTime) {
        this.smcObservationAvgTime = smcObservationAvgTime;
    }

    private void setSmcObservationMinTime(List<GraphPoint> smcObservationMinTime) {
        this.smcObservationMinTime = smcObservationMinTime;
    }

    private void setSmcObservationMaxTime(List<GraphPoint> smcObservationMaxTime) {
        this.smcObservationMaxTime = smcObservationMaxTime;
    }

    public void setSmcGlobalAvgStep(double smcGlobalAvgStep) {
        this.smcGlobalAvgStep = smcGlobalAvgStep;
    }

    public void setSmcGlobalAvgTime(double smcGlobalAvgTime) {
        this.smcGlobalAvgTime = smcGlobalAvgTime;
    }

    public void setSmcObservationValueStep(List<GraphPoint> smcObservationvalueStep) {
        this.smcObservationValueStep = smcObservationvalueStep;
    }

    public void setSmcObservationValueTime(List<GraphPoint> smcObservationvalueTime) {
        this.smcObservationValueTime = smcObservationvalueTime;
    }

    public List<GraphPoint> getSmcObservationAvgStep() {
        return smcObservationAvgStep;
    }

    public List<GraphPoint> getSmcObservationMinStep() {
        return smcObservationMinStep;
    }

    public List<GraphPoint> getSmcObservationMaxStep() {
        return smcObservationMaxStep;
    }

    public List<GraphPoint> getSmcObservationAvgTime() {
        return smcObservationAvgTime;
    }

    public List<GraphPoint> getSmcObservationMinTime() {
        return smcObservationMinTime;
    }

    public List<GraphPoint> getSmcObservationMaxTime() {
        return smcObservationMaxTime;
    }

    public double getSmcGlobalAvgStep() {
        return smcGlobalAvgStep;
    }

    public double getSmcGlobalAvgTime() {
        return smcGlobalAvgTime;
    }

    public List<GraphPoint> getSmcObservationValueStep() {
        return smcObservationValueStep;
    }

    public List<GraphPoint> getSmcObservationValueTime() {
        return smcObservationValueTime;
    }
}
