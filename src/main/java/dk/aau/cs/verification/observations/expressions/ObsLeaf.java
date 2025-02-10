package dk.aau.cs.verification.observations.expressions;

public abstract class ObsLeaf extends ObsExpression {
    @Override
    public ObsExprPosition getObjectPosition(int index) {
        return new ObsExprPosition(0, toString().length(), this);
    }

    @Override
    public ObsExprPosition getObjectPosition(ObsExpression expr) {
        if (equals(expr)) {
            return new ObsExprPosition(0, toString().length(), this);
        } 

        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
