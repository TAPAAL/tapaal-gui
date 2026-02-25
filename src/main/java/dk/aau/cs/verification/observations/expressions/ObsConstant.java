package dk.aau.cs.verification.observations.expressions;

public class ObsConstant extends ObsLeaf {
    private final int value;

    public ObsConstant(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public String toXml(boolean legacy) {
        return "<integer-constant>" + value + "</integer-constant>";
    }

    @Override
    public ObsExpression deepCopy() {
        ObsExpression copy = new ObsConstant(value);
        copy.setParent(parent);
        return copy;
    }
}
