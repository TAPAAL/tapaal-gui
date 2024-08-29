package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.pddl.Parameter;

import java.util.Objects;

public class Expression_Successor implements IExpression_Value {
    private Parameter parameter;

    public Expression_Successor(Parameter parameter) {
        this.parameter = parameter;
    }

    public Expression_Successor() {
    }

    public static String getName(Parameter parameter) {
        return parameter.getName() + "_CpnColorSuccessor";
    }

    @Override
    public String toString() {
        return "?" + getName(parameter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression_Successor that = (Expression_Successor) o;
        return Objects.equals(parameter, that.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter);
    }
}