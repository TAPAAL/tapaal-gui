package dk.aau.cs.verification;

public class QuantitativeResult {
    private float smcEstimation;
    private float smcEstimationWidth;

    public QuantitativeResult(float smcEstimation, float smcEstimationWidth) {
        this.smcEstimation = smcEstimation;
        this.smcEstimationWidth = smcEstimationWidth;
    }

    @Override
    public String toString() {
        return "The estimated probability of the property holding is " + smcEstimation + " ± " + smcEstimationWidth;
    }

    public String getProbabilityString() {
        return "P = " + smcEstimation + " ± " + smcEstimationWidth;
    }
}
