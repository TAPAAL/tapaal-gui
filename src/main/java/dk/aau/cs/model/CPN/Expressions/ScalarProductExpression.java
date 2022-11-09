package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class ScalarProductExpression extends ArcExpression {

    private final Integer scalar;
    private ArcExpression expr;

    public ScalarProductExpression(Integer scalar, ArcExpression expr) {
        this.scalar = scalar;
        this.expr = expr;
    }

    //Missing implementation for evaluation - might not be needed
    public ColorMultiset eval(ExpressionContext context) {
        ColorMultiset result = expr.eval(context);
        result.scale(scalar);
        return result;
    }

    public Integer getScalar(){
        return scalar;
    }

    public ArcExpression getExpr(){
        return expr;
    }

    public Integer weight() {
        return scalar * expr.weight();
    }

    @Override
    public boolean containsColor(Color color) {
        return expr.containsColor(color);
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
    public Expression findFirstPlaceHolder() {
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

        return new ExprStringPosition[]{pos};
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

    @Override
    public ArcExpression getExprWithNewColorType(ColorType ct) {
        return new ScalarProductExpression(scalar, expr.getExprWithNewColorType(ct));
    }
}
