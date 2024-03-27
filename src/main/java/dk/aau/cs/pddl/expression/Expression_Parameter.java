package dk.aau.cs.pddl.expression;

import dk.aau.cs.pddl.Parameter;

public class Expression_Parameter implements IExpression {
    private Parameter parameter;
    public Expression_Parameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return "?" + parameter.getName();
    }
}
