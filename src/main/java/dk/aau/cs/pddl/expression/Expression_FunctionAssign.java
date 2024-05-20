package dk.aau.cs.pddl.expression;

public class Expression_FunctionAssign extends Expression_Compare implements IExpression {
    public Expression_FunctionAssign(Expression_FunctionValue func, Expression_IntegerLiteral amount) {
        super(func, ComparisonTypes.eq, amount);
    }
}
