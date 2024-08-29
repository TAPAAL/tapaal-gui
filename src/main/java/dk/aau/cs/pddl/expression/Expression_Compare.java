package dk.aau.cs.pddl.expression;

public class Expression_Compare extends BaseExpression implements IExpression {

    public enum ComparisonTypes{
        gt,
        gteq,
        eq,
        lteq,
        lt,
        neq
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
            case "!=":
                return ComparisonTypes.neq;
        }

        throw new RuntimeException();
    }

    ComparisonTypes type;
    public Expression_Compare(IExpression_Value left, ComparisonTypes type, IExpression_Value right) {
        this.parameters.add(left);
        this.type = type;
        this.parameters.add(right);
    }

    public static IExpression Make_Comparison(IExpression_Value left, ComparisonTypes type, IExpression_Value right) {
        if(type == ComparisonTypes.neq) {
            return new Expression_Not(new Expression_Compare(left, ComparisonTypes.eq, right));
        }
        return new Expression_Compare(left, type, right);
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
            case neq:
                return "!=";
        }

        throw new RuntimeException("Unhandled type: " + this.type);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
