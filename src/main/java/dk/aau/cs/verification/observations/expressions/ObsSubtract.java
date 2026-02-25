package dk.aau.cs.verification.observations.expressions;

public class ObsSubtract extends ObsOperator {
    public ObsSubtract(ObsExpression left, ObsExpression right) {
        super(left, right);
    }

    public ObsSubtract() {}

    @Override
    protected String getOperator() {
        return "-";
    }

    @Override
    public String toXml(boolean legacy) {
        return "<integer-difference>" + left.toXml(legacy) + right.toXml(legacy) + "</integer-difference>";
    }
}
