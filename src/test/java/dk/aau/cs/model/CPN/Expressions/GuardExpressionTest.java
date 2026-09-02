package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.CPN.GuardExpressionParser.GuardExpressionParser;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
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

    @Test
    void parserResolvesIntegerColorAgainstVariableType() throws Exception {
        var network = new TimedArcPetriNetNetwork();
        var red = colorType("red", "1");
        var blue = colorType("blue", "1", "2");
        network.add(red);
        network.add(blue);
        network.add(new Variable("y", blue));

        var guard = GuardExpressionParser.parse("y=1++", network);
        var right = ((EqualityExpression) guard).getRightExpression();
        var constant = ((SuccessorExpression) right).getSuccessorExpression();
        assertEquals(blue, ((UserOperatorExpression) constant).getUserOperator().getColorType());
    }

    @Test
    void validationRepairsAmbiguousIntegerColor() {
        var red = colorType("red", "1");
        var blue = colorType("blue", "1", "2");
        var guard = new EqualityExpression(new VariableExpression(new Variable("y", blue)),
            new SuccessorExpression(new UserOperatorExpression(red.getFirstColor())));

        guard.validateAndInferColorType();

        var constant = ((SuccessorExpression) guard.getRightExpression()).getSuccessorExpression();
        assertEquals(blue, ((UserOperatorExpression) constant).getUserOperator().getColorType());
    }

    private ColorType colorType(String name, String... colors) {
        var type = new ColorType(name);
        type.addColors(java.util.List.of(colors));
        return type;
    }
}
