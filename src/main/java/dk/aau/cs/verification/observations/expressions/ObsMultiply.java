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
}
