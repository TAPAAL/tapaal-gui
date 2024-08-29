package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Expressions.ColorExpression;
import dk.aau.cs.pddl.Parameter;

import java.util.Objects;

public class Expression_Predecessor implements IExpression_Value {
    private Parameter parameter;

    public Expression_Predecessor(Parameter parameter) {
        this.parameter = parameter;
    }

    public Expression_Predecessor() {
    }

    public static String getName(Parameter parameter) {
        return parameter.getName() + "_CpnColorPredecessor";
    }

    @Override
    public String toString() {
        return "?" + getName(parameter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression_Predecessor that = (Expression_Predecessor) o;
        return Objects.equals(parameter, that.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter);
    }
}