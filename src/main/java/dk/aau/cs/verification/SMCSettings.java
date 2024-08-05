package dk.aau.cs.verification;

public class SMCSettings {

    public enum RunBoundType {

        TIMEBOUND, STEPBOUND;

        public String toString() {
            if(this == TIMEBOUND) {
                return "time";
            } else {
                return "steps";
            }
        }
    }

    public RunBoundType boundType;
    public int boundValue;
    public float falsePositives;
    public float falseNegatives;
    public float indifferenceWidth;
    public float confidence;
    public float estimationIntervalWidth;
    public boolean compareToFloat;
    public float geqThan;

    public static SMCSettings Default() {
        SMCSettings settings = new SMCSettings();
        settings.boundType = RunBoundType.TIMEBOUND;
        settings.boundValue = 1000;
        settings.falsePositives = 0.01f;
        settings.falseNegatives = 0.01f;
        settings.indifferenceWidth = 0.05f;
        settings.confidence = 0.95f;
        settings.estimationIntervalWidth = 0.05f;
        settings.compareToFloat = false;
        settings.geqThan = 0.5f;
        return settings;
    }

    // Computes the number of runs needed according to :
    // https://link.springer.com/content/pdf/10.1007/b94790.pdf p.78-79
    // ONLY RELEVANT FOR PROBABILITY ESTIMATION !
    public int chernoffHoeffdingBound() {
        double bound = Math.log(2.0 / (1 - confidence)) / (2.0 * Math.pow(estimationIntervalWidth, 2));
        return (int) Math.ceil(bound);
    }

    // ONLY RELEVANT FOR PROBABILITY ESTIMATION !
    public double estimateTimeFromBenchmark(int runs, double time) {
        return time * ( (double) chernoffHoeffdingBound() / (double) runs );
    }

    // ONLY RELEVANT FOR PROBABILITY ESTIMATION !
    public float estimatePrecisionFromBenchmark(int runs, double time, double timeWanted) {
        double runsNeeded = Math.ceil( (timeWanted / time) * runs);
        return (float) Math.sqrt(
            Math.log(2.0 / (1 - confidence)) / (2.0 * runsNeeded)
        );
    }

}
