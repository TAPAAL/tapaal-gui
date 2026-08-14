package net.tapaal.gui.petrinet.dialog;

import dk.aau.cs.TCTL.AritmeticOperator;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLTermListNode;
import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedTransition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryDialogArithmeticTest {
    @Test
    void arithmeticOperandsUseArithmeticContext() {
        var selected = new TCTLConstNode(0);
        var comparison = new TCTLAtomicPropositionNode(new TCTLPlaceNode("ERKPP"), "=", selected);
        var query = new TCTLEFNode(comparison);

        assertTrue(QueryDialog.isInsideArithmetic(query, selected));
        assertFalse(QueryDialog.isInsideArithmetic(query, comparison));
    }

    @Test
    void arithmeticSearchRejectsTransitions() {
        assertTrue(QueryDialog.isTransition(new TimedTransition("r10")));
        assertTrue(QueryDialog.isTransition(new SharedTransition("shared")));
        assertFalse(QueryDialog.isTransition(new LocalTimedPlace("ERKPP")));
    }

    @Test
    void arithmeticListsSerializeWithoutMissingOperator() {
        var sum = expression(
            new TCTLPlaceNode("HouseConstruction1", "p17"), "+",
            new TCTLPlaceNode("HouseConstruction1", "p13"));
        var comparison = new TCTLAtomicPropositionNode(
            new TCTLPlaceNode("HouseConstruction1", "Finished"), "=", sum);

        String xml = new CTLQueryVisitor().getXMLQueryFor(new TCTLEFNode(comparison), "query", false);

        assertTrue(xml.contains("<integer-eq>"));
        assertTrue(xml.contains("<integer-sum>"));
        assertFalse(xml.contains("MISSING_OPERATOR"));
    }

    @Test
    void selectingAnOperatorReplacesIt() {
        var expression = expression(new TCTLPlaceNode("P"), "+", new TCTLConstNode(3));

        var edit = QueryDialog.createArithmeticEdit(expression, "*");

        assertSame(expression, edit.target());
        assertEquals("P * 3", edit.replacement().toString());
    }

    @Test
    void repeatedOperatorsStayFlat() {
        assertEquals("2 + <*> + <*> + <*>", pressFirstPlaceholder(new TCTLConstNode(2), "+", 3).toString());
        assertEquals("2 * <*> * <*> * <*>", pressFirstPlaceholder(new TCTLConstNode(2), "*", 3).toString());
        assertEquals("2 - <*> - <*> - <*>", pressFirstPlaceholder(new TCTLConstNode(2), "-", 3).toString());
    }

    @Test
    void lowerPrecedenceOperatorGroupsSelectedMultiplicationOperand() {
        var product = pressFirstPlaceholder(new TCTLConstNode(2), "*", 1);
        var selected = (TCTLAbstractStateProperty)product.findFirstPlaceHolder();

        assertEquals("2 * (<*> + <*>)", press(product, selected, "+").toString());
    }

    @Test
    void placeAndConstantReplacementSelectTheRemainingPlaceholder() {
        assertReplacementSelectsRemainingPlaceholder(new TCTLPlaceNode("P"));
        assertReplacementSelectsRemainingPlaceholder(new TCTLPlaceNode("TAPN1", "P", "dot"));
        assertReplacementSelectsRemainingPlaceholder(new TCTLConstNode(4));
    }

    @Test
    void subtractionGroupsTheFirstOperandInAnAdditionList() {
        var sum = pressFirstPlaceholder(new TCTLConstNode(0), "+", 2);
        var selected = (TCTLAbstractStateProperty)sum.findFirstPlaceHolder();

        assertEquals("0 + (<*> - <*>) + <*>", press(sum, selected, "-").toString());
    }

    @Test
    void additionGroupsTheFirstOperandInASubtractionList() {
        var difference = pressFirstPlaceholder(new TCTLConstNode(0), "-", 2);
        var selected = (TCTLAbstractStateProperty) difference.findFirstPlaceHolder();

        assertEquals("0 - (<*> + <*>) - <*>", press(difference, selected, "+").toString());
    }

    @Test
    void subtractionOnLastAdditionOperandGroupsOnlyThatOperand() {
        var sum = pressFirstPlaceholder(new TCTLConstNode(2), "+", 2);
        var properties = ((TCTLTermListNode) sum).getProperties();
        var selected = properties.get(properties.size() - 1);

        assertEquals("2 + <*> + (<*> - <*>)", press(sum, selected, "-").toString());
    }

    private static void assertReplacementSelectsRemainingPlaceholder(TCTLAbstractStateProperty replacement) {
        var selected = new TCTLStatePlaceHolder();
        var remaining = new TCTLStatePlaceHolder();
        var difference = expression(selected, "-", remaining);
        TCTLAbstractProperty result = expression(new TCTLConstNode(2), "*", difference)
            .replace(selected, replacement);

        assertSame(remaining, QueryDialog.selectionAfterReplacement(result, replacement));
    }

    private static TCTLAbstractStateProperty pressFirstPlaceholder(
        TCTLAbstractStateProperty expression, String operator, int times
    ) {
        for (int i = 0; i < times; ++i) {
            var selected = expression.containsPlaceHolder()
                ? (TCTLAbstractStateProperty)expression.findFirstPlaceHolder()
                : expression;
            expression = press(expression, selected, operator);
        }

        return expression;
    }

    private static TCTLAbstractStateProperty press(
        TCTLAbstractStateProperty expression, TCTLAbstractStateProperty selected, String operator
    ) {
        var edit = QueryDialog.createArithmeticEdit(selected, operator);
        return (TCTLAbstractStateProperty)expression.replace(edit.target(), edit.replacement());
    }

    private static TCTLTermListNode expression(Object... parts) {
        List<TCTLAbstractStateProperty> properties = new ArrayList<>();
        for (var part : parts) {
            properties.add(part instanceof String
                ? new AritmeticOperator((String) part)
                : (TCTLAbstractStateProperty) part);
        }

        return new TCTLTermListNode(properties);
    }
}
