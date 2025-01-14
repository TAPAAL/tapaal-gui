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
    public String toXml() {
        return "<integer-sum>" + left.toXml() + right.toXml() + "</integer-sum>";
    }
}
