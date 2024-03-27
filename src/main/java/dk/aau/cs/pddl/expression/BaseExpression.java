package dk.aau.cs.pddl.expression;

import java.util.ArrayList;

public abstract class BaseExpression {
    public abstract String getName();

    protected ArrayList<IExpression> parameters = new ArrayList<>();

    public ArrayList<IExpression> getParameters() {
        return parameters;
    }

    public void addParameter(IExpression expression) {
        this.parameters.add(expression);
    }

    @Override
    public String toString() {
        String[] stringParams = (String[])getParameters().stream().map(IExpression::toString).toArray();

        return "(" + getName() + " " + String.join(" ", stringParams) + ")";
    }
}
