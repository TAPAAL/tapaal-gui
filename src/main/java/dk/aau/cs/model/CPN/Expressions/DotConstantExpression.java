package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class DotConstantExpression extends UserOperatorExpression {

    public DotConstantExpression() {
        super(ColorType.COLORTYPE_DOT.getFirstColor());
    }

    public List<Color> eval(ExpressionContext context) {
        return Collections.singletonList(ColorType.COLORTYPE_DOT.getFirstColor());
    }

    @Override
    public boolean containsColor(Color color) {
        return color.getColorName().equals("dot");
    }

    @Override
    public ColorExpression updateColor(Color color, ColorType newColorType) {
        //It should not be possible to remove the dot color
        return this;
    }

    @Override
    public boolean hasVariable(List<Variable> variables) {
        return false;
    }

    public ColorType getColorType() {
        return ColorType.COLORTYPE_DOT;
    }
    @Override
    public ColorExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }
    @Override
    public ColorExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
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
    public ColorExpression copy() {
        return null;
    }

    @Override
    public DotConstantExpression deepCopy() {
        return new DotConstantExpression();
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

    }

    @Override
    public String toString(){
        return "dot";
    }

    @Override
    public boolean isComparable(ColorExpression otherExpr){
        otherExpr = otherExpr.getBottomColorExpression();
        return otherExpr instanceof DotConstantExpression;
    }

    @Override
    public ColorExpression getBottomColorExpression() {
        return this;
    }

    @Override
    public Vector<ColorType> getColorTypes() {
        return new Vector<>(Collections.singletonList(ColorType.COLORTYPE_DOT));
    }
}
