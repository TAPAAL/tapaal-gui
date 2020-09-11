package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.DotConstant;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;

public class DotConstantExpression extends ColorExpression {

    public Color eval(ExpressionContext context) {
        return DotConstant.getInstance();
    }

    @Override
    public boolean hasColor(Color color) {
        return color.getColorName() == "dot";
    }

    @Override
    public ColorExpression replace(Expression object1, Expression object2) {
        if (this == object1 && object2 instanceof ColorExpression) {
            ColorExpression obj2 = (ColorExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            return this;
        }
    }

    @Override
    public Expression copy() {
        return null;
    }

    @Override
    public boolean containsPlaceHolder() {
        return false;
    }

    @Override
    public ColorExpression findFirstPlaceHolder() {
        return null;
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        return exprValues;
    }

    @Override
    public String toString(){
        return "dot";
    }
}
