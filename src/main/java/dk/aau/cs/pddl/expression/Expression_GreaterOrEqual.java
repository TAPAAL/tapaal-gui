package dk.aau.cs.pddl.expression;

public class Expression_GreaterOrEqual extends BaseExpression implements IExpression {

    public Expression_GreaterOrEqual(IExpression left, IExpression right) {
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
