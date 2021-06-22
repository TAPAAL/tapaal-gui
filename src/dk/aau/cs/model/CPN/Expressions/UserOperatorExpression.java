package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class UserOperatorExpression extends ColorExpression {

    private Color userOperator;

    public Color getUserOperator(){
        return this.userOperator;
    }

    public UserOperatorExpression(Color userOperator) {
        this.userOperator = userOperator;
    }

    public List<Color> eval(ExpressionContext context) {
        return Arrays.asList(userOperator);
    }

    @Override
    public boolean containsColor(Color color) {
        return userOperator.equals(color);
    }

    @Override
    public ColorExpression updateColor(Color color, ColorType newColorType) {
        if (userOperator.equals(color)) {
            return null;
        }
        return this;
    }

    @Override
    public boolean hasVariable(List<Variable> variables) {
        return false;
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
    public ColorExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
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
    public ColorExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }
    @Override
    public ColorExpression copy() {
        return new UserOperatorExpression(userOperator);
    }

    @Override
    public ColorExpression deepCopy() {
        return new UserOperatorExpression(userOperator.deepCopy());
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
    public void getValues(ExprValues exprValues) {
        exprValues.addColor(userOperator);
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
    @Override
    public boolean isComparable(ColorExpression otherExpr){
        otherExpr = otherExpr.getButtomColorExpression();
        if(otherExpr instanceof TupleExpression){
            return false;
        }
        if(otherExpr instanceof VariableExpression) {
            VariableExpression otherUserOpExpression = (VariableExpression) otherExpr;
            if (!userOperator.getColorType().equals(otherUserOpExpression.getVariable().getColorType())) {
                return false;
            }else {
                return true;
            }
        }
        if(!(otherExpr instanceof UserOperatorExpression)){
            return false;
        }
        UserOperatorExpression otherUserOpExpression = (UserOperatorExpression) otherExpr;
        if(!userOperator.getColorType().equals(otherUserOpExpression.userOperator.getColorType())){
            return false;
        }
        return true;
    }
    @Override
    public ColorExpression getButtomColorExpression(){
        return this;
    }

    @Override
    public Vector<ColorType> getColorTypes(){
        return new Vector<>(Arrays.asList(userOperator.getColorType()));
    }


}
