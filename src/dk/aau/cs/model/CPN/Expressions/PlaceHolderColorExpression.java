package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlaceHolderColorExpression extends ColorExpression implements PlaceHolderExpression {

    @Override
    public List<Color> eval(ExpressionContext context) {
        return Arrays.asList();
    }

    @Override
    public boolean hasColor(Color color) {
        return false;
    }

    @Override
    public ColorType getColorType(List<ColorType> colorTypes) {
        return null;
    }

    @Override
    public ColorExpression replace(Expression object1, Expression object2) {
        if (this == object1 && object2 instanceof ColorExpression) {
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
        return new PlaceHolderColorExpression();
    }

    @Override
    public ColorExpression deepCopy() {
        return new PlaceHolderColorExpression();
    }

    @Override
    public boolean containsPlaceHolder() {
        return true;
    }

    @Override
    public ColorExpression findFirstPlaceHolder() {
        return this;
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        return exprValues;
    }

    @Override
    public String toString() {
        return "<+>";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PlaceHolderColorExpression) {
            return  true;
        }
        else {
            return false;
        }
    }
}
