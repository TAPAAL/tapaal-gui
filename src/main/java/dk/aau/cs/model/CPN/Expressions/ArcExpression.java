package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ColorMultiset;
import dk.aau.cs.model.CPN.ColorType;

public abstract class ArcExpression extends Expression {
    protected ArcExpression parent;
    protected boolean hadParentheses = false;

    public ArcExpression() {}

    public ArcExpression(ArcExpression otherExpr) {
        this.parent = otherExpr.parent;
    }

    @Override
    public abstract ArcExpression replace(Expression object1, Expression object2,boolean replaceAllInstances);

    @Override
    public abstract ArcExpression replace(Expression object1, Expression object2);

    public ArcExpression getParent() { return parent; }

    public void setParent(ArcExpression parent) { this.parent = parent; }

    @Override
    public abstract ArcExpression copy();

    public abstract ArcExpression deepCopy();

    @Override
    public abstract Expression findFirstPlaceHolder();

    public abstract ColorMultiset eval(ExpressionContext context);

    public abstract Integer weight();

    public abstract ArcExpression getExprWithNewColorType(ColorType ct);

    public abstract ArcExpression getExprConverted(ColorType oldCt, ColorType newCt);

    public void hadParentheses(boolean hadParentheses) {
        this.hadParentheses = hadParentheses;
    }

    public boolean addParentheses() {
        return (parent != null && parent.getClass() != this.getClass()) || hadParentheses;
    }
}
