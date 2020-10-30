package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

import java.util.List;
import java.util.Set;

public abstract class ColorExpression extends Expression {

    protected ColorExpression parent;

    public ColorExpression() {

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

    public abstract boolean hasColor(Color color);
    public abstract ColorExpression updateColor(Color color, ColorType newColorType);

    public abstract boolean hasVariable(List<Variable> variables);

    public abstract ColorType getColorType(List<ColorType> colortypes);
}
