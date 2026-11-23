package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

import java.util.List;
import java.util.Set;
import java.util.Vector;

public abstract class ColorExpression extends Expression {

    protected ColorExpression parent;
    protected int index = -1;
    protected ColorType colorType;

    public ColorExpression(ColorType colorType) {
        this.colorType = colorType;
    }

    @Override
    public abstract ColorExpression copy();

    public abstract ColorExpression deepCopy();

    public abstract ColorExpression findFirstPlaceHolder();

    public abstract boolean containsPlaceHolder();

    public abstract ColorExpression replace(Expression object1, Expression object2,boolean replaceAllInstances);
    public abstract ColorExpression replace(Expression object1, Expression object2);

    public void setParent(ColorExpression parent) {this.parent = parent;}

    public void getVariables(Set<Variable> variables) {
    }
    // This function might only be needed in the derived classes
    public abstract List<Color> eval(ExpressionContext context);

    public abstract boolean containsColor(Color color);
    public abstract ColorExpression updateColor(Color color, ColorType newColorType);

    public abstract boolean hasVariable(List<Variable> variables);

    public ColorType getColorType() { return colorType; }

    public abstract boolean isComparable(ColorExpression otherExpr);

    public abstract ColorExpression getBottomColorExpression();

    public abstract Vector<ColorType> getColorTypes();

    public ColorExpression getParent(){
        return this.parent;
    }

    public void setIndex(int index){
        this.index = index;
    }
    public int getIndex(){
        return index;
    }

    public abstract ColorExpression getExprWithNewColorType(ColorType ct);

    // Rebind this expression from oldCt to newCt. A matching type name is enough:
    // ColorType.equals also compares the color list, so a rename or an added color
    // would not match. Tuples override this and walk their constituents.
    public ColorExpression getExprConverted(ColorType oldCt, ColorType newCt) {
        if (colorType != null && colorType.getName().equals(oldCt.getName())) {
            return getExprWithNewColorType(newCt);
        }
        return this;
    }

    public static ColorExpression resolveAgainst(ColorExpression expression, ColorExpression other) {
        var colorType = other.getColorType();
        if (colorType == null) {
            return expression;
        }

        var converted = expression.getExprWithNewColorType(colorType);
        return colorType.equals(converted.getColorType()) ? converted : expression;
    }
}
