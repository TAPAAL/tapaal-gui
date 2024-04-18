package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;

public class Expression_ColorLiteral implements IExpression_Value {
    private Color color;

    public Expression_ColorLiteral(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "?" + color.getName();
    }
}