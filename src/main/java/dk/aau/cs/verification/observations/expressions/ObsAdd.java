package dk.aau.cs.verification.observations.expressions;

public class ObsAdd extends ObsOperator {
    public ObsAdd(ObsExpression left, ObsExpression right) {
        super(left, right);
    }

    public ObsAdd() {}

    @Override
    protected String getOperator() {
        return "+";
    }

    @Override
    public String toXml(boolean legacy) {
        return "<integer-sum>" + left.toXml(legacy) + right.toXml(legacy) + "</integer-sum>";
    }
}
