package dk.aau.cs.verification;

public class SMCStats extends Stats {

    private int executedRuns;
    private int validRuns;
    private float averageRunTime;
    private float averageRunLength;
    private float verificationTime;

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
            averageRunLength;
    }

}
