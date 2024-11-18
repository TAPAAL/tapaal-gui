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
    public String toXml() {
        return "<obs-constant value=\"" + value + "\"/>";
    }

    @Override
    public ObsExpression deepCopy() {
        return new ObsConstant(value);
    }
}
