package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuardExpressionTest {
    @Test
    void guardMustContainAVariable() {
        var type = colorType("A", "a");
        var guard = new EqualityExpression(
            new UserOperatorExpression(type.getFirstColor()),
            new UserOperatorExpression(type.getFirstColor())
        );

        assertThrows(IllegalArgumentException.class, guard::validateAndInferColorType);
    }

    @Test
    void guardTypeIsInferredFromVariableAndPropagatedToChildren() {
        var type = colorType("A", "a");
        var variable = new Variable("x", type);
        var comparison = new EqualityExpression(
            new VariableExpression(variable),
            new UserOperatorExpression(type.getFirstColor())
        );
        var guard = new NotExpression(comparison);

        guard.validateAndInferColorType();

        assertEquals(type, guard.getColorType());
        assertEquals(type, comparison.getColorType());
    }

    @Test
    void allGuardVariablesMustHaveTheSameType() {
        var firstType = colorType("A", "a");
        var secondType = colorType("B", "b");
        var guard = new EqualityExpression(
            new VariableExpression(new Variable("x", firstType)),
            new VariableExpression(new Variable("y", secondType))
        );

        assertThrows(IllegalArgumentException.class, guard::validateAndInferColorType);
    }

    private ColorType colorType(String name, String color) {
        var type = new ColorType(name);
        type.addColor(color);
        return type;
    }
}
