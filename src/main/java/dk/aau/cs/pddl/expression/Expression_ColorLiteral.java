package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;

import java.util.Objects;

public class Expression_ColorLiteral implements IExpression_Value {
    private Color color;
    private String name;

    public Expression_ColorLiteral(Color color) {
        this.color = color;

        String name = color.getName();
        if(name == "dot")
            name = "dot_obj";
        this.name = name;
    }

    public Expression_ColorLiteral(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
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