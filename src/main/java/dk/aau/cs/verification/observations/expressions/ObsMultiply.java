package dk.aau.cs.verification.observations.expressions;

public class ObsMultiply extends ObsOperator {
    public ObsMultiply(ObsExpression left, ObsExpression right) {
        super(left, right);
    }

    public ObsMultiply() {}

    @Override
    protected String getOperator() {
        return "*";
    }

    @Override
    public String toXml(boolean legacy) {
        return "<integer-product>" + left.toXml(legacy) + right.toXml(legacy) + "</integer-product>";
    }
}
