package dk.aau.cs.verification.observations.expressions;

public class ObsAdd extends ObsOperator {
    public ObsAdd(ObsExpression left, ObsExpression right) {
        super(left, right);
    }

    public ObsAdd() {
        super();
    }

    @Override
    protected String getOperator() {
        return "+";
    }

    @Override
    public String toXml() {
        return "<obs-add>" + left.toXml() + right.toXml() + "</obs-add>";
    }
}
