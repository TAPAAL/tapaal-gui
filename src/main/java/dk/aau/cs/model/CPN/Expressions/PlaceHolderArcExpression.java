package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class PlaceHolderArcExpression extends ArcExpression implements PlaceHolderExpression {

    @Override
    public ColorMultiset eval(ExpressionContext context) {
        return null;
    }

    @Override
    public Integer weight() {
        return null;
    }

    @Override
    public boolean containsColor(Color color) {
        return false;
    }

    @Override
    public ArcExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (this == object1 && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            return this;
        }
    }
    @Override
    public ArcExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public ArcExpression copy() {
        return new PlaceHolderArcExpression();
    }

    @Override
    public ArcExpression deepCopy() {
        return new PlaceHolderArcExpression();
    }

    @Override
    public boolean containsPlaceHolder() {
        return true;
    }

    @Override
    public Expression findFirstPlaceHolder() {
        return this;
    }

    @Override
    public void getValues(ExprValues exprValues) {
    }

    @Override
    public void getVariables(Set<Variable> variables) {

    }

    @Override
    public String toString() {
        return "<->";
    }

    @Override
    public ArcExpression getExprWithNewColorType(ColorType ct) {
        return deepCopy();
    }

    @Override
    public ArcExpression getExprConverted(ColorType oldCt, ColorType newCt) {
        return deepCopy();
    }
}
