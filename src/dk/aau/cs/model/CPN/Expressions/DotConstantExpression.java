package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.DotConstant;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Arrays;
import java.util.List;

public class DotConstantExpression extends ColorExpression {

    public List<Color> eval(ExpressionContext context) {
        return Arrays.asList(DotConstant.getInstance());
    }

    @Override
    public boolean hasColor(Color color) {
        return color.getColorName() == "dot";
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

    @Override
    public ColorType getColorType(List<ColorType> colorTypes) {
        return getColorType();
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
    public ExprValues getValues(ExprValues exprValues) {
        return exprValues;
    }

    @Override
    public String toString(){
        return "dot";
    }
}
