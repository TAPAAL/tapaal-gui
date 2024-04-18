package dk.aau.cs.pddl.expression;

public class Expresssion_Decrement extends BaseExpression implements IExpression {

    public Expresssion_Decrement(Expression_FunctionValue func, Expression_IntegerLiteral amount) {
        this.parameters.add(func);
        this.parameters.add(amount);
    }

    @Override
    public String getName() {
        return "decrement";
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
