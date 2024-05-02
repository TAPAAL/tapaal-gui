package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;

import java.util.Objects;

public class Expression_ColorLiteral implements IExpression_Value {
    private Color color;

    public Expression_ColorLiteral(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression_ColorLiteral that = (Expression_ColorLiteral) o;
        return Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color);
    }
}