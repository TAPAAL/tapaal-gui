package dk.aau.cs.verification.observations.expressions;

public abstract class ObsLeaf implements ObsExpression {
    @Override
    public ObsExprPosition getObjectPosition(int index) {
        return new ObsExprPosition(0, toString().length(), this);
    }

    @Override
    public boolean isOperator() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public boolean isPlaceHolder() {
        return false;
    }

    public boolean isPlace() {
        return false;
    }
}
