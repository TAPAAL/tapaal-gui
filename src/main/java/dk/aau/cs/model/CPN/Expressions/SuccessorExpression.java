package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.*;

public class SuccessorExpression extends ColorExpression {
    private final ColorExpression color;

    public ColorExpression getSuccessorExpression() {
        return this.color;
    }

    public SuccessorExpression(ColorExpression color) {
<<<<<<< HEAD
=======
        super(color.colorType);
>>>>>>> origin/cpn
        this.color = color;
    }

    //Missing implementation for evaluation - might not be needed
    public List<Color> eval(ExpressionContext context) {
        return Collections.singletonList(color.eval(context).get(0).successor());
    }

    @Override
    public boolean containsColor(Color color) {
        return this.color.containsColor(color);
    }

    @Override
    public ColorExpression updateColor(Color color, ColorType newColorType) {
        ColorExpression expr = this.color.updateColor(color, newColorType);
        if (expr != null) {
            return new PredecessorExpression(expr);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasVariable(List<Variable> variables) {
        return this.color.hasVariable(variables);
    }

    @Override
<<<<<<< HEAD
    public ColorType getColorType(List<ColorType> colorTypes) {
        return color.getColorType(colorTypes);
    }

    @Override
=======
>>>>>>> origin/cpn
    public ColorExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (object1.toString().equals(this.toString()) && object2 instanceof ColorExpression) {
            ColorExpression obj2 = (ColorExpression)object2;
            obj2.setParent(parent);
            return obj2;
        }
        else
            return new SuccessorExpression(color.replace(object1, object2,replaceAllInstances));
    }
    @Override
    public ColorExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }
    @Override
    public ColorExpression copy() {
        return new SuccessorExpression(color);
    }

    @Override
    public ColorExpression deepCopy() {
        return new SuccessorExpression(color.deepCopy());
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
    public void getValues(ExprValues exprValues) {
        color.getValues(exprValues);
    }

    public void getVariables(Set<Variable> variables) {
        color.getVariables(variables);
    }

    public String toString() {
        return color.toString() + "++";
    }

    @Override
    public ExprStringPosition[] getChildren() {
<<<<<<< HEAD

        ExprStringPosition pos = new ExprStringPosition(0, color.toString().length() - 2 , color);
=======
        ExprStringPosition pos = new ExprStringPosition(0, color.toString().length(), color);
>>>>>>> origin/cpn
        return new ExprStringPosition[]{pos};
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }

    @Override
    public boolean isComparable(ColorExpression otherExpr){
        return color.isComparable(otherExpr);
    }

    @Override
    public ColorExpression getBottomColorExpression(){
        return color.getBottomColorExpression();
    }

    @Override
<<<<<<< HEAD
    public Vector<ColorType> getColorTypes(){
=======
    public Vector<ColorType> getColorTypes() {
>>>>>>> origin/cpn
        return color.getColorTypes();
    }
}
