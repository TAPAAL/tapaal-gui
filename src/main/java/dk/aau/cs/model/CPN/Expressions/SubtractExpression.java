package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class SubtractExpression extends ArcExpression {

    private ArcExpression left;
    private ArcExpression right;

    public SubtractExpression(ArcExpression left, ArcExpression right) {
        this.left = left;
        this.right = right;
    }

    public SubtractExpression(SubtractExpression otherExpr) {
        super(otherExpr);
        this.left = otherExpr.left.copy();
        this.right = otherExpr.right.copy();
    }


    public ArcExpression getLeftExpression() {return this.left;}

    public ArcExpression getRightExpression() {return this.right;}

    //Missing implementation for evaluation - might not be needed
    public ColorMultiset eval(ExpressionContext context) {
        ColorMultiset result = left.eval(context);
        result.subAll(right.eval(context));
        return result;
    }

    //discuss implementation with group
    public Integer weight() {
        return null;
    }

    @Override
    public boolean containsColor(Color color) {
        return left.containsColor(color) || right.containsColor(color);
    }

    @Override
    public ArcExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (object1 == this && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression) object2;
            obj2.setParent(parent);
            return obj2;
        } else {
            left = left.replace(object1, object2,replaceAllInstances);
            right = right.replace(object1, object2,replaceAllInstances);
            return this;
        }
    }
    @Override
    public ArcExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public ArcExpression copy() {
        return new SubtractExpression(left, right);
    }

    @Override
    public ArcExpression deepCopy() {
        return new SubtractExpression(left.deepCopy(), right.deepCopy());
    }

    @Override
    public boolean containsPlaceHolder() {
        return left.containsPlaceHolder() || right.containsPlaceHolder();
    }

    @Override
<<<<<<< HEAD
    public ArcExpression findFirstPlaceHolder() {
=======
    public Expression findFirstPlaceHolder() {
>>>>>>> origin/cpn
        if (left.containsPlaceHolder()) {
            return left.findFirstPlaceHolder();
        }
        else {
            return right.findFirstPlaceHolder();
        }
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

    public String toString() {
        return left.toString() + " - " + right.toString();
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
        start = endPrev + 3;
        end = start + right.toString().length();
        pos = new ExprStringPosition(start, end, right);
        children[1] = pos;

        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SubtractExpression) {
            SubtractExpression expr = (SubtractExpression) o;
            return (left.equals(expr.left) && right.equals(expr.right));
        }
        return false;
    }

}
