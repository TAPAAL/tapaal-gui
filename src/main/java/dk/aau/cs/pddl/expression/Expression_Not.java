package dk.aau.cs.pddl.expression;

public class Expression_Not extends BaseExpression implements IExpression {

    public Expression_Not(IExpression exp) {
        this.parameters.add(exp);
    }

    @Override
    public String getName() {
        return "not";
    }

}
