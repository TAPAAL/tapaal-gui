package dk.aau.cs.pddl;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.pddl.expression.*;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import java.util.ArrayList;

public class QueryParser {
    public Expression_And expression = new Expression_And();

    public void parseQuery(TAPNQuery query) throws UnhandledExpressionType {
        TCTLAbstractProperty prop = query.getProperty();
        parseProperty(prop);
    }

    private void parseProperty(TCTLAbstractProperty property) throws UnhandledExpressionType {
        var type = property.getClass();

        if (type == TCTLEFNode.class) {
            parseProperty((TCTLEFNode) property);
        } else if (type == TCTLAndListNode.class) {
            parseProperty((TCTLAndListNode) property);
        } else if (type == TCTLAtomicPropositionNode.class) {
            parseProperty((TCTLAtomicPropositionNode) property);
        } else {
            throw new UnhandledExpressionType();
        }

    }

    private void parseProperty(TCTLEFNode property) throws UnhandledExpressionType {
        parseProperty(property.getProperty());

    }

    private void parseProperty(TCTLAndListNode property) throws UnhandledExpressionType {
        for (var p : property.getProperties()) {
            parseProperty(p);
        }
    }

    private void parseProperty(TCTLAtomicPropositionNode property) throws UnhandledExpressionType {
        TCTLAbstractStateProperty leftAbstract = property.getLeft();
        TCTLAbstractStateProperty rightAbstract = property.getRight();

        TCTLPlaceNode place;
        TCTLConstNode value;
        String operator;

        // place op value
        if(leftAbstract.getClass() == TCTLPlaceNode.class && rightAbstract.getClass() == TCTLConstNode.class) {
            place = (TCTLPlaceNode)leftAbstract;
            value = (TCTLConstNode)rightAbstract;
            operator = property.getOp();
        }
        // value op place
        else if(leftAbstract.getClass() == TCTLConstNode.class && rightAbstract.getClass() == TCTLPlaceNode.class) {
            value = (TCTLConstNode)leftAbstract;
            place = (TCTLPlaceNode)rightAbstract;

            switch (property.getOp()) {
                case "<":
                    operator = ">=";
                    break;
                case "<=":
                    operator = ">";
                    break;
                case "=":
                case "==":
                    operator = "=";
                    break;
                case ">=":
                    operator = "<";
                    break;
                case ">":
                    operator = "<=";
                    break;
                default:
                    throw new UnhandledExpressionType();
            }
        }
        else {
            throw new UnhandledExpressionType();
        }

        expression.addParameter(
            new Expression_FunctionCompare(new Expression_FunctionValue(place.getPlace(), new ArrayList<>()),
            Expression_FunctionCompare.getType(operator),
            new Expression_IntegerLiteral(value.getConstant())
        ));

    }

}


