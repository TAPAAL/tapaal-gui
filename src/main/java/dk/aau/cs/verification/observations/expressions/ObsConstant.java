package dk.aau.cs.verification.observations.expressions;

public class ObsConstant extends ObsLeaf {
    private final double value;

    public ObsConstant(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public String toXml() {
        return "<real-constant>" + value + "</real-constant>";
    }

    @Override
    public ObsExpression deepCopy() {
        ObsExpression copy = new ObsConstant(value);
        copy.setParent(parent);
        return copy;
    }
}
