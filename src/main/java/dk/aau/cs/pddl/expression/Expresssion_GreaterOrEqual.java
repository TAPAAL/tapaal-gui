package dk.aau.cs.pddl.expression;

import dk.aau.cs.pddl.Parameter;

public class Expresssion_GreaterOrEqual extends BaseExpression implements IExpression {

    public Expresssion_GreaterOrEqual(IExpression left, IExpression right) {
        this.parameters.add(left);
        this.parameters.add(right);
    }

    @Override
    public String getName() {
        return ">=";
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
