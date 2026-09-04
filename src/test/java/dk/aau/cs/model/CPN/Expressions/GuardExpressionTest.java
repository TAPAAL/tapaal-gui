package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.CPN.GuardExpressionParser.GuardExpressionParser;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void comparisonAllowsVariablesWithDifferentTypes() {
        var firstType = colorType("A", "a");
        var secondType = colorType("B", "b");
        var guard = new EqualityExpression(
            new VariableExpression(new Variable("x", firstType)),
            new VariableExpression(new Variable("y", secondType))
        );

        assertDoesNotThrow(guard::validateAndInferColorType);
        assertEquals(firstType, guard.getColorType());
        assertEquals(firstType, guard.getLeftExpression().getColorType());
        assertEquals(secondType, guard.getRightExpression().getColorType());
    }

    @Test
    void parserAllowsAllComparisonsAcrossDifferentTypes() {
        var network = new TimedArcPetriNetNetwork();
        var firstType = colorType("A", "1");
        var secondType = colorType("B", "1", "2");
        network.add(firstType);
        network.add(secondType);
        network.add(new Variable("x", firstType));
        network.add(new Variable("y", secondType));

        for (var operator : new String[]{"<", "<=", "=", "!=", ">=", ">"}) {
            assertDoesNotThrow(() -> GuardExpressionParser.parse("x++" + operator + "y++", network));
        }
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

    @Test
    void parserResolvesIntegerColorFromVariableOnEitherSide() throws Exception {
        var network = new TimedArcPetriNetNetwork();
        var other = colorType("other", "4");
        var range = colorType("range", "1", "2", "3", "4", "5");
        network.add(other);
        network.add(range);
        network.add(new Variable("x", range));

        var variableFirst = (EqualityExpression)GuardExpressionParser.parse("x++=4", network);
        var firstConstant = (UserOperatorExpression)variableFirst.getRightExpression();
        assertEquals(range, firstConstant.getUserOperator().getColorType());

        var variableLast = (EqualityExpression)GuardExpressionParser.parse("4=x++", network);
        var lastConstant = (UserOperatorExpression)variableLast.getLeftExpression();
        assertEquals(range, lastConstant.getUserOperator().getColorType());
    }

    @Test
    void enumerationOperandsKeepTheirDeclaredTypes() throws Exception {
        var network = new TimedArcPetriNetNetwork();
        var firstType = colorType("E", "a0", "a1");
        var secondType = colorType("F", "b0", "b1");
        network.add(firstType);
        network.add(secondType);
        network.add(new Variable("e", firstType));
        network.add(new Variable("f", secondType));

        assertDoesNotThrow(() -> GuardExpressionParser.parse("e=f", network));
        var guard = (EqualityExpression)GuardExpressionParser.parse("e=b0", network);
        assertEquals(firstType, guard.getLeftExpression().getColorType());
        assertEquals(secondType, guard.getRightExpression().getColorType());
    }

    @Test
    void everyComparisonMustContainAVariable() {
        var type = colorType("A", "a", "b");
        var variableComparison = new EqualityExpression(
            new VariableExpression(new Variable("x", type)),
            new UserOperatorExpression(type.getFirstColor())
        );
        var constantComparison = new EqualityExpression(
            new UserOperatorExpression(type.getFirstColor()),
            new UserOperatorExpression(type.getColors().get(1))
        );

        var guard = new AndExpression(variableComparison, constantComparison);
        assertThrows(IllegalArgumentException.class, guard::validateAndInferColorType);
    }

    private ColorType colorType(String name, String... colors) {
        var type = new ColorType(name);
        type.addColors(java.util.List.of(colors));
        return type;
    }
}
