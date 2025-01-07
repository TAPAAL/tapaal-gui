package dk.aau.cs.verification.observations.expressions;

public class ObsMultiply extends ObsOperator {
    public ObsMultiply(ObsExpression left, ObsExpression right) {
        super(left, right);
    }

    public ObsMultiply() {
        super();
    }

    @Override
    protected String getOperator() {
        return "*";
    }

    @Override
    public String toXml() {
        return "<integer-product>" + left.toXml() + right.toXml() + "</integer-product>";
    }
}
