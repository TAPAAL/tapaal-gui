package dk.aau.cs.pddl.expression;

public class Expression_Or extends BaseExpressionNAry implements IExpression {

    public Expression_Or(IExpression... params) {
        super(params);
    }

    @Override
    public String getName() {
        return "or";
    }

    @Override
    public String toString() {
        return super.toString("\n\t\t\t");
    }

}
