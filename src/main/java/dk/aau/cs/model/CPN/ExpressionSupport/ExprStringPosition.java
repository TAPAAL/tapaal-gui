package dk.aau.cs.model.CPN.ExpressionSupport;

import dk.aau.cs.model.CPN.Expressions.Expression;

public class ExprStringPosition {

    private int startIndex;
    private int endIndex;
    private final Expression object;

    public ExprStringPosition(int startIndex, int endIndex, Expression object) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.object = object;
    }

    public int getStart() {
        return startIndex;
    }

    public int getEnd() {
        return endIndex;
    }

    public ExprStringPosition addOffset(int offset) {
        startIndex += offset;
        endIndex += offset;
        return this;
    }

    public Expression getObject() {
        return object;
    }

}
