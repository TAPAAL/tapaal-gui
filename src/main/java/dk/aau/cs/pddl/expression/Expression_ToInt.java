package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.pddl.Parameter;

public class Expression_ToInt extends BaseExpression implements IExpression_Value {
    public Expression_ToInt(IExpression_Value value) {
        this.parameters.add(value);
    }

    public Expression_ToInt(Color color) {
        this(new Expression_ColorLiteral(color));
    }

    @Override
    public String getName() {
        return "toInt";
    }
}
