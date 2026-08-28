package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;

import java.util.HashSet;
import java.util.Set;

public abstract class GuardExpression extends Expression {
    protected GuardExpression parent;
    protected ColorType colorType;

    public GuardExpression getParent() { return parent; }

    public void setParent(GuardExpression parent) { this.parent = parent; }

    public ColorType getColorType() { return colorType; }

    public void setColorType(ColorType colorType) { this.colorType = colorType; }

    public void setColorTypeRecursively(ColorType colorType) { setColorType(colorType); }

    @Override
    public abstract GuardExpression replace(Expression object1, Expression object2, boolean replaceAllInstances);

    @Override
    public abstract GuardExpression replace(Expression object1, Expression object2);

    @Override
    public abstract GuardExpression copy();

    @Override
    public abstract Expression findFirstPlaceHolder();

    public abstract void getVariables(Set<Variable> variables);

    public void validateAndInferColorType() {
        var variables = new HashSet<Variable>();
        getVariables(variables);
        if (variables.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one variable in the guard expression.");
        }

        ColorType inferredColorType = null;
        for (var variable : variables) {
            if (inferredColorType == null) {
                inferredColorType = variable.getColorType();
            } else if (!inferredColorType.equals(variable.getColorType())) {
                throw new IllegalArgumentException("All variables in a guard expression must have the same color type.");
            }
        }

        setColorTypeRecursively(inferredColorType);
    }

    public abstract Boolean eval(ExpressionContext context);
}
