package dk.aau.cs.verification.observations.expressions;

public class ObsConstant extends ObsLeaf {
    private final float value;

    public ObsConstant(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Float.toString(value);
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
