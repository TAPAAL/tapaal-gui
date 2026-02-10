package dk.aau.cs.verification.observations;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.verification.observations.expressions.ObsExpression;
import dk.aau.cs.verification.observations.expressions.ObsPlaceHolder;

public class Observation {
    private String name;
    private boolean isEnabled = true;

    private ObsExpression expression = new ObsPlaceHolder();

    public Observation(String name) {
        this.name = name;
    }

    public Observation(String name, ObsExpression expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public ObsExpression getExpression() {
        return expression;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExpression(ObsExpression expression) {
        this.expression = expression;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean replacePlace(TimedPlace toReplace, TimedPlace replacement, TimedArcPetriNet tapn) {
        BooleanResult affected = new BooleanResult(false);
        ObsExpression newExpr = expression.replacePlace(toReplace, replacement, tapn, affected);
        if (newExpr != expression) {
            expression = newExpr;
        }

        return affected.result();
    }

    @Override
    public String toString() {
        return name + ": " + expression.toString();
    }

    public String toXml() {
        return "<watch name=\"" + name + "\" isEnabled=\"" + isEnabled + "\">" + expression.toXml() + "</watch>";
    }
}
