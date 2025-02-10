package dk.aau.cs.verification.observations.expressions;

public abstract class ObsExpression {
    protected ObsExpression parent;

    public abstract ObsExpression deepCopy();
    public abstract ObsExprPosition getObjectPosition(int index);
    public abstract ObsExprPosition getObjectPosition(ObsExpression expr);
    public abstract String toXml();

    public boolean isOperator() {
        return false;
    }

    public boolean isLeaf() {
        return false;
    }

    public boolean isPlaceHolder() {
        return false;
    }
    
    public boolean isPlace() {
        return false;
    }

    public void setParent(ObsExpression parent) {
        this.parent = parent;
    }

    public ObsExpression getParent() {
        return parent;
    }
}
