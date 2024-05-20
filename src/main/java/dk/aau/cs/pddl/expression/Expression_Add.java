package dk.aau.cs.pddl.expression;

public class Expression_Add extends BaseExpression implements IExpression_Value {

    public Expression_Add(IExpression_Value left, IExpression_Value right) {
        this.parameters.add(left);
        this.parameters.add(right);
    }

    @Override
    public String getName() {
        return "+";
    }

}
