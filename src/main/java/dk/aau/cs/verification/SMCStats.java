package dk.aau.cs.verification;

public class SMCStats extends Stats {

    private int executedRuns;
    private int validRuns;
    private float averageRunTime;
    private float averageRunLength;

    public SMCStats(int executedRuns, int validRuns, float averageTime, float averageLength) {
        super(-1,-1,-1);
        this.executedRuns = executedRuns;
        this.validRuns = validRuns;
        this.averageRunTime = averageTime;
        this.averageRunLength = averageLength;
    }

    @Override
    public String toString() {
        return "Number of runs executed: " +
            executedRuns +
            System.getProperty("line.separator") +
            "Number of valid runs: " +
            validRuns +
            System.getProperty("line.separator") +
            "Average run time: " +
            averageRunTime +
            System.getProperty("line.separator") +
            "Average run length: " +
            averageRunLength;
    }

}
