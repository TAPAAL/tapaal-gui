package dk.aau.cs.pddl.expression;

public class Expression_FunctionCompare extends BaseExpression implements IExpression {

    public enum ComparisonTypes{
        gt,
        gteq,
        eq,
        lteq,
        lt
    }
    public static ComparisonTypes getType(String str) {
        switch (str) {
            case ">":
                return ComparisonTypes.gt;
            case ">=":
                return ComparisonTypes.gteq;
            case "=":
            case "==":
                return ComparisonTypes.eq;
            case "<=":
                return ComparisonTypes.lteq;
            case "<":
                return ComparisonTypes.lt;
        }

        throw new RuntimeException();
    }

    ComparisonTypes type;
    public Expression_FunctionCompare(Expression_FunctionValue func, ComparisonTypes type, Expression_IntegerLiteral amount) {
        this.parameters.add(func);
        this.type = type;
        this.parameters.add(amount);
    }

    @Override
    public String getName() {
        switch (this.type) {
            case gt:
                return ">";
            case gteq:
                return ">=";
            case eq:
                return "=";
            case lteq:
                return "<=";
            case lt:
                return "<";
        }

        throw new RuntimeException("Unhandled type: " + this.type);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
