package dk.aau.cs.pddl.expression;

public class Expression_FunctionEq extends BaseExpression implements IExpression {

    public Expression_FunctionEq(Expression_FunctionValue func, Expression_IntegerLiteral amount) {
        this.parameters.add(func);
        this.parameters.add(amount);
    }

    @Override
    public String getName() {
        return "=";
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
