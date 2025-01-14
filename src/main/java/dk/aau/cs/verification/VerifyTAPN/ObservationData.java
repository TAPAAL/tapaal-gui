package dk.aau.cs.verification.VerifyTAPN;

import java.util.List;

import pipe.gui.graph.GraphPoint;

public class ObservationData {
    private List<GraphPoint> smcObservationAvgStep;
    private List<GraphPoint> smcObservationMinStep;
    private List<GraphPoint> smcObservationMaxStep;
    private List<GraphPoint> smcObservationAvgTime;
    private List<GraphPoint> smcObservationMinTime;
    private List<GraphPoint> smcObservationMaxTime;

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
}
