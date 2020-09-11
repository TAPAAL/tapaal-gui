package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorMultiset;

public abstract class ArcExpression extends Expression {

    protected ArcExpression parent;

    public ArcExpression() {

    }

    public abstract ArcExpression replace(Expression object1, Expression object2);

    public ArcExpression getParent() {return parent;}

    public void setParent(ArcExpression parent) {this.parent = parent; }

    @Override
    public abstract ArcExpression copy();

    @Override
    public abstract ArcExpression findFirstPlaceHolder();


    public abstract ColorMultiset eval(ExpressionContext context);
    public abstract void expressionType();
    public abstract Integer weight();

    public abstract ArcExpression removeColorFromExpression(Color color);
}
