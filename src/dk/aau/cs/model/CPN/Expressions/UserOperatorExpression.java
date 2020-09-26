package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;

import java.util.List;

public class UserOperatorExpression extends ColorExpression {

    private Color userOperator;

    public Color getUserOperator(){
        return this.userOperator;
    }

    public UserOperatorExpression(Color userOperator) {
        this.userOperator = userOperator;
    }

    public Color eval(ExpressionContext context) {
        return userOperator;
    }

    @Override
    public boolean hasColor(Color color) {
        return userOperator.equals(color);
    }

    @Override
    public ColorType getColorType(List<ColorType> colorTypes) {
        return userOperator.getColorType();
    }

    @Override
    public String toString() {
        return userOperator.toString();
    }

    @Override
    public ColorExpression replace(Expression object1, Expression object2) {
        if (object1 == this && object2 instanceof  ColorExpression) {
            ColorExpression obj2 = (ColorExpression)object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            return this;
        }
    }

    @Override
    public ColorExpression copy() {
        return new UserOperatorExpression(userOperator);
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
        exprValues.addColor(userOperator);
        return exprValues;
    }

    @Override
    public boolean isSimpleProperty() {return false;}

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[0];
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserOperatorExpression) {
            UserOperatorExpression expr = (UserOperatorExpression) o;
            return userOperator.equals(expr.userOperator);
        }
        return false;
    }

}
