package dk.aau.cs.pddl.expression;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.pddl.Parameter;

public class Expression_ToInt extends BaseExpression implements IExpression_Value {
    public Expression_ToInt(Parameter param) {
        this.parameters.add(param);
    }

    public Expression_ToInt(Expression_ColorLiteral color) {
        this.parameters.add(color);
    }

    public Expression_ToInt(Color color) {
        this(new Expression_ColorLiteral(color));
    }

    @Override
    public String getName() {
        return "toInt";
    }
}
