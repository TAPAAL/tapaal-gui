package dk.aau.cs.pddl.expression;

public class Expression_IntegerLiteral implements IExpression {
    private int value;

    public int getValue() {
        return value;
    }

    public Expression_IntegerLiteral(int value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
