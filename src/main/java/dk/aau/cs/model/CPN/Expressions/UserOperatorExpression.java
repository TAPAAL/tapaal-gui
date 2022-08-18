package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class UserOperatorExpression extends ColorExpression {

    private final Color userOperator;

    public Color getUserOperator(){
        return this.userOperator;
    }

    public UserOperatorExpression(Color userOperator) {
        this(userOperator, userOperator.getColorType());
    }
    public UserOperatorExpression(Color userOperator, ColorType colorType) {
        super(colorType);
        this.userOperator = userOperator;
    }

    public List<Color> eval(ExpressionContext context) {
        return Collections.singletonList(userOperator);
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
        return new ExprStringPosition[0];
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
        otherExpr = otherExpr.getBottomColorExpression();
        if(otherExpr instanceof TupleExpression){
            return false;
        }
        if(otherExpr instanceof VariableExpression) {
            VariableExpression otherUserOpExpression = (VariableExpression) otherExpr;
            return userOperator.getColorType().equals(otherUserOpExpression.getVariable().getColorType());
        }
        if(!(otherExpr instanceof UserOperatorExpression)){
            return false;
        }
        UserOperatorExpression otherUserOpExpression = (UserOperatorExpression) otherExpr;
        return userOperator.getColorType().equals(otherUserOpExpression.userOperator.getColorType());
    }
    @Override
    public ColorExpression getBottomColorExpression(){
        return this;
    }

    @Override
    public Vector<ColorType> getColorTypes(){
        return new Vector<>(Collections.singletonList(userOperator.getColorType()));
    }


}
