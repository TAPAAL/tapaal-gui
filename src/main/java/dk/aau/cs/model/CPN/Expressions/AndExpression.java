package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class AndExpression extends GuardExpression {
    private  GuardExpression left;
    private GuardExpression right;

    public AndExpression(GuardExpression left, GuardExpression right) {
        this.left = left;
        this.right = right;
    }
    public GuardExpression getLeftExpression(){
        return this.left;
    }
    public GuardExpression getRightExpression(){
        return this.right;
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2, boolean replaceAllInstances) {
        if (this == object1 && object2 instanceof GuardExpression) {
            GuardExpression obj2 = (GuardExpression)object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            left = left.replace(object1, object2, replaceAllInstances);
            right = right.replace(object1, object2, replaceAllInstances);
            return this;
        }
    }

    @Override
    public boolean containsPlaceHolder() {
        return left.containsPlaceHolder() || right.containsPlaceHolder();
    }

    @Override
    public Expression findFirstPlaceHolder() {
        if (left.containsPlaceHolder()) {
            return left.findFirstPlaceHolder();
        }
        else if (right.containsPlaceHolder()) {
            return right.findFirstPlaceHolder();
        }
        return null;
    }

    @Override
    public void getValues(ExprValues exprValues) {
        left.getValues(exprValues);
        right.getValues(exprValues);
    }

    public void getVariables(Set<Variable> variables) {
        left.getVariables(variables);
        right.getVariables(variables);
    }

    //Missing implementation for evaluation - might not be needed
    public Boolean eval(ExpressionContext context) {
        return null;
    }

    @Override
    public boolean containsColor(Color color) {
        return left.containsColor(color) || right.containsColor(color);
    }

    @Override
    public String toString() {
        return left.toString() + " and " + right.toString();
    }

    @Override
    public boolean isSimpleProperty() {return false; }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[2];
        int endPrev = 0;

        int start = 0;
        int end = 0;

        end = start + left.toString().length();
        endPrev = end;
        ExprStringPosition pos = new ExprStringPosition(start, end, left);
        children[0] = pos;
        start = endPrev + 5;
        end = start + right.toString().length();
        pos = new ExprStringPosition(start, end, right);
        children[1] = pos;

        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AndExpression) {
            AndExpression expr = (AndExpression) o;
            return (left.equals(expr.left) && right.equals(expr.right));
        }
        return false;
    }

    @Override
    public GuardExpression copy() {
       return new AndExpression(left, right);
    }

}
