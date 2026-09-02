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

        normalizeColorExpressions(this);
        setColorTypeRecursively(inferredColorType);
    }

    private static void normalizeColorExpressions(GuardExpression expression) {
        if (expression instanceof LeftRightGuardExpression comparison) {
            var left = comparison.getLeftExpression();
            var right = comparison.getRightExpression();
            if (!left.isComparable(right)) {
                var converted = ColorExpression.resolveAgainst(right, left);
                expression.replace(right, converted, false);
                if (!left.isComparable(converted)) expression.replace(left, ColorExpression.resolveAgainst(left, converted), false);
            }

            if (!comparison.getLeftExpression().isComparable(comparison.getRightExpression())) {
                throw new IllegalArgumentException(left + " is not comparable to " + right);
            }
        }
        
        for (var child : expression.getChildren()) {
            if (child.getObject() instanceof GuardExpression guard) normalizeColorExpressions(guard);
        }
    }

    public abstract Boolean eval(ExpressionContext context);
}
