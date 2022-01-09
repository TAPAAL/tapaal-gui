package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class PlaceHolderGuardExpression extends GuardExpression implements PlaceHolderExpression {

    public PlaceHolderGuardExpression() {}

    public boolean isSimple() {return true;}

    @Override
    public GuardExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (this == object1 && object2 instanceof GuardExpression) {
            GuardExpression obj2 = (GuardExpression)object2;
            obj2.setParent(parent);
            return obj2;
        }
        else  {
            return this;
        }
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }
    @Override
    public GuardExpression copy() {
        return new PlaceHolderGuardExpression();
    }

    @Override
    public boolean containsPlaceHolder() {
        return true;
    }

    @Override
    public GuardExpression findFirstPlaceHolder() {
        return this;
    }

    @Override
    public void getValues(ExprValues exprValues) {

    }

    @Override
    public void getVariables(Set<Variable> variables) {

    }

    @Override
    public Boolean eval(ExpressionContext context) {
        return null;
    }

    @Override
    public boolean containsColor(Color color) {
        return false;
    }

    @Override
    public String toString() {
        return "<*>";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlaceHolderGuardExpression;
    }

}
