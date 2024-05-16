package dk.aau.cs.pddl.expression;

public class Expression_FunctionEq extends Expression_FunctionCompare implements IExpression {
    public Expression_FunctionEq(Expression_FunctionValue func, Expression_IntegerLiteral amount) {
        super(func, ComparisonTypes.eq, amount);
    }
}
