package dk.aau.cs.verification;

public class SMCStats extends Stats {

    private int executedRuns;
    private int validRuns;
    private float averageRunTime;
    private float averageRunLength;
    private float verificationTime;
    private float averageValidRunTime = -1.0f;
    private float averageValidRunLength = -1.0f;
    private float validRunTimeStdDev = -1.0f;
    private float validRunLengthStdDev = -1.0f;

    public SMCStats(int executedRuns, int validRuns, float averageTime, float averageLength) {
        super(-1,-1,-1);
        this.executedRuns = executedRuns;
        this.validRuns = validRuns;
        this.averageRunTime = averageTime;
        this.averageRunLength = averageLength;
        this.verificationTime = -1.0f;
    }

    public SMCStats(int executedRuns, int validRuns, float averageTime, float averageLength, float verificationTime) {
        super(-1,-1,-1);
        this.executedRuns = executedRuns;
        this.validRuns = validRuns;
        this.averageRunTime = averageTime;
        this.averageRunLength = averageLength;
        this.verificationTime = verificationTime;
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

    @Override
    public String toString() {
        return "Number of runs executed: " +
            executedRuns +
            System.getProperty("line.separator") +
            "Number of valid runs: " +
            validRuns +
            System.getProperty("line.separator") +
            "Average run duration: " +
            averageRunTime +
            System.getProperty("line.separator") +
            "Average transitions fired per run: " +
            averageRunLength +
            (averageValidRunTime >= 0 ?
                System.getProperty("line.separator") +
                "Average valid run duration: " +
                averageValidRunTime : "") +
            (validRunTimeStdDev >= 0 ?
                System.getProperty("line.separator") +
                    "Valid runs duration standard deviation: " +
                    validRunTimeStdDev : "") +
            (averageValidRunLength >= 0 ?
                System.getProperty("line.separator") +
                "Average transitions fired per valid run: " +
                averageValidRunLength : "") +
            (validRunLengthStdDev >= 0 ?
                System.getProperty("line.separator") +
                    "Valid runs transitions fired standard deviation: " +
                    validRunLengthStdDev : "");
    }

    public float getAverageValidRunTime() {
        return averageValidRunTime;
    }

    public void setAverageValidRunTime(float averageValidRunTime) {
        this.averageValidRunTime = averageValidRunTime;
    }

    public float getAverageValidRunLength() {
        return averageValidRunLength;
    }

    public void setAverageValidRunLength(float averageValidRunLength) {
        this.averageValidRunLength = averageValidRunLength;
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
}
