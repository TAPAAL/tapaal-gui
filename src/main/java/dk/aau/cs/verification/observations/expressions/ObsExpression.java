package dk.aau.cs.verification.observations.expressions;

import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;

public abstract class ObsExpression {
    protected ObsExpression parent;

    public abstract ObsExpression deepCopy();
    public abstract ObsExprPosition getObjectPosition(int index);
    public abstract ObsExprPosition getObjectPosition(ObsExpression expr);
    public abstract String toXml();

    public ObsExpression replacePlace(TimedPlace toReplace, TimedPlace replacement, TimedArcPetriNet tapn, BooleanResult affected) {
        return this;
    }

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

    public boolean containsPlace(TimedPlace place) {
        return false;
    }

    public void setParent(ObsExpression parent) {
        this.parent = parent;
    }

    public ObsExpression getParent() {
        return parent;
    }
}
