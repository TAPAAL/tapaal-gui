package dk.aau.cs.verification.observations.expressions;

public class ObsSubtract extends ObsOperator {
    public ObsSubtract(ObsExpression left, ObsExpression right) {
        super(left, right);
    }

    public ObsSubtract() {
        super();
    }

    @Override
    protected String getOperator() {
        return "-";
    }

    @Override
    public String toXml() {
        return "<obs-subtract>" + left.toXml() + right.toXml() + "</obs-subtract>";
    }
}
