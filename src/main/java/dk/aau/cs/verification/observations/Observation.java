package dk.aau.cs.verification.observations;

import dk.aau.cs.verification.observations.expressions.ObsExpression;
import dk.aau.cs.verification.observations.expressions.ObsPlaceHolder;

public class Observation {
    private String name;

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

    public void setName(String name) {
        this.name = name;
    }

    public void setExpression(ObsExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toXml() {
        return "<watch name=\"" + name + "\">" + expression.toXml() + "</watch>";
    }
}
