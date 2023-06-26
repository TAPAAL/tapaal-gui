package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public abstract class GuardExpression extends Expression {
    protected GuardExpression parent;
    protected ColorType colorType;

    public GuardExpression getParent() { return parent; }

    public void setParent(GuardExpression parent) { this.parent = parent; }

    public ColorType getColorType() { return colorType; }

    @Override
    public abstract GuardExpression replace(Expression object1, Expression object2, boolean replaceAllInstances);

    @Override
    public abstract GuardExpression replace(Expression object1, Expression object2);

    @Override
    public abstract GuardExpression copy();

    @Override
    public abstract Expression findFirstPlaceHolder();

    public abstract void getVariables(Set<Variable> variables);

    public abstract Boolean eval(ExpressionContext context);
}
