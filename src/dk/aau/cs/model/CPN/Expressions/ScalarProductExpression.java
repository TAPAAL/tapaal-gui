package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.List;
import java.util.Set;

public class ScalarProductExpression extends ArcExpression {

    private Integer scalar;
    private ArcExpression expr;

    public ScalarProductExpression(Integer scalar, ArcExpression expr) {
        this.scalar = scalar;
        this.expr = expr;
    }

    public ScalarProductExpression(ScalarProductExpression otherExpr) {
        super(otherExpr);
        this.scalar = otherExpr.scalar;
        this.expr = otherExpr.expr.copy();
    }

    //Missing implementation for evaluation - might not be needed
    public ColorMultiset eval(ExpressionContext context) {
        ColorMultiset result = expr.eval(context);
        result.scale(scalar);
        return result;
    }

    public void expressionType() {

    }

    public Integer weight() {
        return scalar * expr.weight();
    }

    @Override
    public ArcExpression removeColorFromExpression(Color color, ColorType newColorType) {
        if(expr.removeColorFromExpression(color, newColorType) == null){
            return null;
        } else{
            return this;
        }
    }

    @Override
    public ArcExpression removeExpressionVariables(List<Variable> variables) {
        if(expr.removeExpressionVariables(variables) == null) {
            return null;
        } else {
            return this;
        }
    }

    public ArcExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (this == object1 && object2 instanceof ArcExpression) {
            ArcExpression obj2 = (ArcExpression) object2;
            obj2.setParent(parent);
            return obj2;
        } else {
            expr = expr.replace(object1, object2,replaceAllInstances);
            return this;
        }
    }
    @Override
    public ArcExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public ArcExpression copy() {
        return new ScalarProductExpression(scalar, expr);
    }

    @Override
    public ArcExpression deepCopy() {
        return new ScalarProductExpression(scalar, expr.deepCopy());
    }

    @Override
    public boolean containsPlaceHolder() {
        return expr.containsPlaceHolder();
    }

    @Override
    public ArcExpression findFirstPlaceHolder() {
        return expr.findFirstPlaceHolder();
    }

    @Override
    public void getValues(ExprValues exprValues) {
        expr.getValues(exprValues);
    }

    @Override
    public boolean isSimpleProperty() {return false; }

    @Override
    public ExprStringPosition[] getChildren() {
        int start = 4;

        int end = start + expr.toString().length();
        ExprStringPosition pos = new ExprStringPosition(start, end, expr);
        ExprStringPosition[] children = {pos};

        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ScalarProductExpression) {
            ScalarProductExpression expr = (ScalarProductExpression) o;
            return this.expr.equals(expr.expr);
        }
        return false;
    }

    public void getVariables(Set<Variable> variables) {
        expr.getVariables(variables);
    }

    public String toString() {
        return scalar.toString() + " * " + expr.toString();
    }
}
