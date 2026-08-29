package dk.aau.cs.verification;

import java.util.List;
import java.util.Map;

import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.VerifyTAPN.ObservationData;
import pipe.gui.graph.GraphPoint;

public class SMCStats extends Stats {

    private int executedRuns;
    private int validRuns;
    private float averageRunTime;
    private float averageRunLength;
    private float runTimeStdDev = -1.0f;
    private float runLengthStdDev = -1.0f;
    private float verificationTime;
    private float validRunAverageTime = -1.0f;
    private float validRunAverageLength = -1.0f;
    private float validRunTimeStdDev = -1.0f;
    private float validRunLengthStdDev = -1.0f;
    private float violatingRunAverageTime = -1.0f;
    private float violatingRunAverageLength = -1.0f;
    private float violatingRunTimeStdDev = -1.0f;
    private float violatingRunLengthStdDev = -1.0f;

    private List<GraphPoint> cumulativeStepPoints;
    private List<GraphPoint> cumulativeDelayPoints;
    private Map<String, ObservationData> observationDataMap;

    public SMCStats(int executedRuns, int validRuns, float averageTime, float averageLength, float verificationTime, List<GraphPoint> cumulativeStepPoints, List<GraphPoint> cumulativeDelayPoints, Map<String, ObservationData> observationData, List<Tuple<String,Number>> transitionStats, List<Tuple<String,Number>> placeBoundStats) {
        super(-1,-1,-1, transitionStats, placeBoundStats);
        this.executedRuns = executedRuns;
        this.validRuns = validRuns;
        this.averageRunTime = averageTime;
        this.averageRunLength = averageLength;
        this.verificationTime = verificationTime;
        this.cumulativeStepPoints = cumulativeStepPoints;
        this.cumulativeDelayPoints = cumulativeDelayPoints; 
        this.observationDataMap = observationData;
    }

    public float getVerificationTime() {
        return this.verificationTime;
    }

    public int getExecutedRuns() {
        return executedRuns;
    }

    public void setExecutedRuns(int executedRuns) {
        this.executedRuns = executedRuns;
    }

    public int getValidRuns() {
        return this.validRuns;
    }

    public int getViolatingRuns() {
        return this.executedRuns - this.validRuns;
    }

    public float getAverageRunTime() {
        return averageRunTime;
    }

    public float getAverageRunLength() {
        return averageRunLength;
    }

    @Override
    public String toString() {
        return "Number of runs executed: " +
            executedRuns +
            (validRuns >= 0 ? 
                System.getProperty("line.separator") +
                "Number of valid runs: " +
                validRuns : "") +
            System.getProperty("line.separator") +
            "Average run duration: " +
            averageRunTime +
            System.getProperty("line.separator") +
            "Average transitions fired per run: " +
            averageRunLength +
            (validRunAverageTime >= 0 ?
                System.getProperty("line.separator") +
                "Valid run duration (average): " +
                    validRunAverageTime : "") +
            (validRunTimeStdDev >= 0 ?
                System.getProperty("line.separator") +
                    "Valid run duration (standard deviation): " +
                    validRunTimeStdDev : "") +
            (validRunAverageLength >= 0 ?
                System.getProperty("line.separator") +
                "Transitions fired per valid run (average): " +
                    validRunAverageLength : "") +
            (validRunLengthStdDev >= 0 ?
                System.getProperty("line.separator") +
                    "Transitions fired per valid run (standard deviation): " +
                    validRunLengthStdDev : "");
    }

    public float getValidRunAverageTime() {
        return validRunAverageTime;
    }

    public void setValidRunAverageTime(float validRunAverageTime) {
        this.validRunAverageTime = validRunAverageTime;
    }

    public float getValidRunAverageLength() {
        return validRunAverageLength;
    }

    public void setValidRunAverageLength(float validRunAverageLength) {
        this.validRunAverageLength = validRunAverageLength;
    }

    public float getViolatingRunAverageTime() {
        return violatingRunAverageTime;
    }

    public void setViolatingRunAverageTime(float violatingRunAverageTime) {
        this.violatingRunAverageTime = violatingRunAverageTime;
    }

    public float getViolatingRunAverageLength() {
        return violatingRunAverageLength;
    }

    public void setViolatingRunAverageLength(float violatingRunAverageLength) {
        this.violatingRunAverageLength = violatingRunAverageLength;
    }

    public List<GraphPoint> getCumulativeStepPoints() {
        return cumulativeStepPoints;
    }

    public List<GraphPoint> getCumulativeDelayPoints() {
        return cumulativeDelayPoints;
    }

    public Map<String, ObservationData> getObservationDataMap() {
        return observationDataMap;
    }

    public float getValidRunTimeStdDev() {
        return validRunTimeStdDev;
    }

    public void setValidRunTimeStdDev(float validRunTimeStdDev) {
        this.validRunTimeStdDev = validRunTimeStdDev;
    }

    public float getValidRunLengthStdDev() {
        return validRunLengthStdDev;
    }

    public void setValidRunLengthStdDev(float validRunLengthStdDev) {
        this.validRunLengthStdDev = validRunLengthStdDev;
    }

    public float getViolatingRunTimeStdDev() {
        return violatingRunTimeStdDev;
    }

    public void setViolatingRunTimeStdDev(float violatingRunTimeStdDev) {
        this.violatingRunTimeStdDev = violatingRunTimeStdDev;
    }

    public float getViolatingRunLengthStdDev() {
        return violatingRunLengthStdDev;
    }

    public void setViolatingRunLengthStdDev(float violatingRunLengthStdDev) {
        this.violatingRunLengthStdDev = violatingRunLengthStdDev;
    }

    public float getRunTimeStdDev() {
        return runTimeStdDev;
    }

    public void setRunTimeStdDev(float runTimeStdDev) {
        this.runTimeStdDev = runTimeStdDev;
    }

    public float getRunLengthStdDev() {
        return runLengthStdDev;
    }

    public void setRunLengthStdDev(float runLengthStdDev) {
        this.runLengthStdDev = runLengthStdDev;
    }
}
