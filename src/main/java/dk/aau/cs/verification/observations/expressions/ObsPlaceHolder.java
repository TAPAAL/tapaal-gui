package dk.aau.cs.verification.observations.expressions;

public class ObsPlaceHolder extends ObsLeaf {
    @Override
    public String toString() {
        return "<*>";
    }

    @Override
    public String toXml(boolean legacy) {
        throw new UnsupportedOperationException("Cannot convert a placeholder to XML");
    }

    @Override
    public ObsExpression deepCopy() {
        return new ObsPlaceHolder();
    }

    @Override
    public boolean isPlaceHolder() {
        return true;
    }
}