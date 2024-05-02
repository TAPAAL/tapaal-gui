package dk.aau.cs.pddl.expression;

public class Expression_Increment extends BaseExpression implements IExpression {

    public Expression_Increment(Expression_FunctionValue func, Expression_IntegerLiteral amount) {
        this.parameters.add(func);
        this.parameters.add(amount);
    }

    @Override
    public String getName() {
        return "increment";
    }

}
