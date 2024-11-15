package dk.aau.cs.verification.observations.expressions;

public class ObsPlaceHolder extends ObsLeaf {
    @Override
    public String toString() {
        return "<*>";
    }

    @Override
    public ObsExpression copy() {
        return new ObsPlaceHolder();
    }

    @Override
    public boolean isPlaceHolder() {
        return true;
    }
}