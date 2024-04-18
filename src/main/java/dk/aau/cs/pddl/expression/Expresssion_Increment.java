package dk.aau.cs.pddl.expression;

public class Expresssion_Increment extends BaseExpression implements IExpression {

    public Expresssion_Increment(Expression_FunctionValue func, Expression_IntegerLiteral amount) {
        this.parameters.add(func);
        this.parameters.add(amount);
    }

    @Override
    public String getName() {
        return "increment";
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
