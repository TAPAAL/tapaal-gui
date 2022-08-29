package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ColorMultiset;

public abstract class ArcExpression extends Expression {

    protected ArcExpression parent;

    public ArcExpression() {

    }

    public ArcExpression(ArcExpression otherExpr) {
        this.parent = otherExpr.parent;
    }

    public abstract ArcExpression replace(Expression object1, Expression object2,boolean replaceAllInstances);
    public abstract ArcExpression replace(Expression object1, Expression object2);


    public ArcExpression getParent() {return parent;}

    public void setParent(ArcExpression parent) {this.parent = parent; }

    @Override
    public abstract ArcExpression copy();

    public abstract ArcExpression deepCopy();

    @Override
<<<<<<< HEAD
    public abstract ArcExpression findFirstPlaceHolder();
=======
    public abstract Expression findFirstPlaceHolder();
>>>>>>> origin/cpn


    public abstract ColorMultiset eval(ExpressionContext context);

    public abstract Integer weight();
}
