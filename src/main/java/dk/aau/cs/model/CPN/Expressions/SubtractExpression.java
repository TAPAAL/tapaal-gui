package dk.aau.cs.model.CPN.Expressions;

import java.util.Set;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

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
    public ArcExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public ArcExpression replace(Expression object1, Expression object2, boolean replaceAllInstances) {
        if (object1 == this && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression)object2;
            obj2.setParent(parent);
            return obj2;
        } else {
            ArcExpression newLeft = left.replace(object1, object2, replaceAllInstances);
            ArcExpression newRight = right.replace(object1, object2, replaceAllInstances);
            
            if (newLeft != left) {
                left = newLeft;
                left.setParent(this);
            }

            if (newRight != right) {
                right = newRight;
                right.setParent(this);
            }

            return this;
        }
    }

    @Override
    public ArcExpression copy() {
        return new SubtractExpression(left, right);
    }

    @Override
    public ArcExpression deepCopy() {
        ArcExpression copy = new SubtractExpression(left.deepCopy(), right.deepCopy());
        copy.setParent(parent);
        return copy;
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

    @Override
    public boolean addParentheses() {
        return parent != null;
    }

    @Override
    public String toString() {
        if (addParentheses()) {
            return "(" + left.toString() + " - " + right.toString() + ")";
        }

        return left.toString() + " - " + right.toString();
    }

    @Override
    public boolean isSimpleProperty() {return false; }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[2];
        
        int leftStart = addParentheses() ? 1 : 0;
        int leftEnd = leftStart + left.toString().length();
        children[0] = new ExprStringPosition(leftStart, leftEnd, left);

        int rightStart = leftEnd + 3;
        int rightEnd = rightStart + right.toString().length();
        children[1] = new ExprStringPosition(rightStart, rightEnd, right);

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

    @Override
    public ArcExpression getExprWithNewColorType(ColorType ct) {
        return new SubtractExpression(left.getExprWithNewColorType(ct), right.getExprWithNewColorType(ct));
    }

    @Override
    public ArcExpression getExprConverted(ColorType oldCt, ColorType newCt) {
        return new SubtractExpression(left.getExprConverted(oldCt, newCt), right.getExprConverted(oldCt, newCt));
    }
}
