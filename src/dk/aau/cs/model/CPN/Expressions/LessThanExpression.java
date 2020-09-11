package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class LessThanExpression extends GuardExpression {

    private ColorExpression left;
    private ColorExpression right;

    public LessThanExpression(ColorExpression left, ColorExpression right) {
        this.left = left;
        this.right = right;
    }
    public ColorExpression getLeftExpression() {
        return this.left;
    }
    public ColorExpression getRightExpression() {
        return this.right;
    }
    @Override
    public GuardExpression removeColorFromExpression(Color color) {
        if(left.hasColor(color) || right.hasColor(color)){
            return null;
        } else{
            return this;
        }
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2) {
        if (this == object1 && object2 instanceof GuardExpression) {
            GuardExpression obj2 = (GuardExpression)object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            left = (ColorExpression) left.replace(object1, object2);
            right = (ColorExpression) right.replace(object1, object2);
            return this;
        }
    }

    @Override
    public GuardExpression copy() {
        return new LessThanExpression(left, right);
    }

    @Override
    public boolean containsPlaceHolder() {
        return left.containsPlaceHolder() || right.containsPlaceHolder();
    }

    @Override
    public Expression findFirstPlaceHolder() {
        if(left.containsPlaceHolder()){
            return left.findFirstPlaceHolder();
        } else if(right.containsPlaceHolder()){
            return right.findFirstPlaceHolder();
        } else{
            return null;
        }
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        exprValues = left.getValues(exprValues);
        exprValues = right.getValues(exprValues);
        return exprValues;
    }

    @Override
    public void getVariables(Set<Variable> variables) {
        left.getVariables(variables);
        right.getVariables(variables);
    }

    @Override     //Missing implementation for evaluation - might not be needed
    public Boolean eval(ExpressionContext context) {
        return null;
    }

    @Override
    public String toString() {
        return left.toString() + " < " + right.toString();
    }

    @Override
    public boolean isSimpleProperty() {return false; }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[2];
        int endPrev = 0;
        boolean wasPrevSimple = false;

        int start = 0;
        int end = 0;

        end = start + left.toString().length();
        endPrev = end;
        ExprStringPosition pos = new ExprStringPosition(start, end, left);
        children[0] = pos;
        start = endPrev + 3;
        end = start + right.toString().length();
        pos = new ExprStringPosition(start, end, right);
        children[1] = pos;

        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LessThanExpression) {
            LessThanExpression expr = (LessThanExpression) o;
            return (left.equals(expr.left) && right.equals(expr.right));
        }
        return false;
    }
}
