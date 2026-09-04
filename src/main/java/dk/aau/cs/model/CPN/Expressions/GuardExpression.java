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
        normalizeColorExpressions(this);
    }

    private static ColorType normalizeColorExpressions(GuardExpression expression) {
        if (expression instanceof LeftRightGuardExpression comparison) {
            var left = comparison.getLeftExpression();
            var right = comparison.getRightExpression();
            var leftVariables = new HashSet<Variable>();
            var rightVariables = new HashSet<Variable>();
            left.getVariables(leftVariables);
            right.getVariables(rightVariables);
            if (leftVariables.isEmpty() && rightVariables.isEmpty()) {
                throw new IllegalArgumentException("There must be at least one variable in each comparison.");
            }

            if (leftVariables.isEmpty()) {
                expression.replace(left, ColorExpression.resolveAgainst(left, right), false);
            } else if (rightVariables.isEmpty()) {
                expression.replace(right, ColorExpression.resolveAgainst(right, left), false);
            }

            if (!comparison.getLeftExpression().isComparable(comparison.getRightExpression())) {
                throw new IllegalArgumentException(left + " is not comparable to " + right);
            }

            var inferredColorType = leftVariables.isEmpty() ? right.getColorType() : left.getColorType();
            expression.setColorType(inferredColorType);
            return inferredColorType;
        }

        ColorType inferredColorType = null;
        for (var child : expression.getChildren()) {
            if (child.getObject() instanceof GuardExpression guard) {
                var childColorType = normalizeColorExpressions(guard);
                if (inferredColorType == null) {
                    inferredColorType = childColorType;
                }
            }
        }

        expression.setColorType(inferredColorType);
        return inferredColorType;
    }

    public abstract Boolean eval(ExpressionContext context);
}
