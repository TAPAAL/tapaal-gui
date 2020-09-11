package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class SuccessorExpression extends ColorExpression {
    private ColorExpression color;

    public ColorExpression getSuccessorExpression() {
        return this.color;
    }

    public SuccessorExpression(ColorExpression color) {
        this.color = color;
    }

    //Missing implementation for evaluation - might not be needed
    public Color eval(ExpressionContext context) {
        return color.eval(context).successor();
    }

    @Override
    public boolean hasColor(Color color) {
        return this.color.hasColor(color);
    }

    @Override
    public ColorExpression replace(Expression object1, Expression object2) {
        if (object1 == this && object2 instanceof ColorExpression) {
            ColorExpression obj2 = (ColorExpression)object2;
            obj2.setParent(parent);
            return obj2;
        }
        else
            return this.color.replace(object1, object2);
    }

    @Override
    public ColorExpression copy() {
        return new SuccessorExpression(color);
    }

    @Override
    public boolean containsPlaceHolder() {
        return color.containsPlaceHolder();
    }

    @Override
    public ColorExpression findFirstPlaceHolder() {
        return color.findFirstPlaceHolder();
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        exprValues = color.getValues(exprValues);
        return exprValues;
    }

    public void getVariables(Set<Variable> variables) {
        color.getVariables(variables);
    }

    public String toString() {
        return color.toString() + "++";
    }

    @Override
    public ExprStringPosition[] getChildren() {

        ExprStringPosition pos = new ExprStringPosition(0, color.toString().length() - 2 , color);
        ExprStringPosition[] children = {pos};
        return children;
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }
}
