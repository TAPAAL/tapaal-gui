package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class NotExpression extends GuardExpression {
    GuardExpression expr;

    public NotExpression(GuardExpression expr) {
        this.expr = expr;
    }

    public GuardExpression getExpression() {
        return this.expr;
    }
    @Override
    public boolean containsColor(Color color) {
        return expr.containsColor(color);
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (this == object1 && object2 instanceof GuardExpression) {
            GuardExpression obj2 = (GuardExpression) object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            expr = expr.replace(object1, object2,replaceAllInstances);
            return this;
        }
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public GuardExpression copy() {
        return new NotExpression(expr);
    }

    @Override
    public boolean containsPlaceHolder() {
        return expr.containsPlaceHolder();
    }

    @Override
    public Expression findFirstPlaceHolder() {
        return expr.findFirstPlaceHolder();
    }

    @Override
    public void getValues(ExprValues exprValues) {
        expr.getValues(exprValues);
    }

    public void getVariables(Set<Variable> variables) {
        expr.getVariables(variables);
    }

    public Boolean eval(ExpressionContext context) {
        return !expr.eval(context);
    }

    @Override
    public String toString() {
        return  "!(" + expr.toString() + ")";
    }

    @Override
    public ExprStringPosition[] getChildren() {
        int start = 2;
        int end = start + expr.toString().length()-1;
        ExprStringPosition pos = new ExprStringPosition(start, end, expr);
        return new ExprStringPosition[]{pos};
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }
}

