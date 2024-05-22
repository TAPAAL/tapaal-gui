package dk.aau.cs.pddl;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.pddl.expression.*;
import net.tapaal.gui.petrinet.verification.TAPNQuery;

import java.util.ArrayList;

public class QueryParser {
    private TimedArcPetriNet petriNet;
    private int xmlIndex = 0;
    public int getXmlIndex() {
        return xmlIndex;
    }
    public void setXmlIndex(int xmlIndex) {
        this.xmlIndex = xmlIndex;
    }

    public Expression_And expression = new Expression_And();

    public QueryParser(TimedArcPetriNet petriNet, int xmlIndex) {
        this.petriNet = petriNet;
        setXmlIndex(xmlIndex);
    }

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
                case "!=":
                    operator = "!=";
                    break;
                default:
                    throw new UnhandledExpressionType();
            }
        }
        else {
            throw new UnhandledExpressionType();
        }


        var ptPlace = this.petriNet.getPlaceByName(place.getPlace());
        var typeValuesIterator = util.getAllPossibleColors(ptPlace).iterator();

        ArrayList<Color> color = typeValuesIterator.next();
        IExpression_Value exp = new Expression_FunctionValue(ptPlace, color);

        while(typeValuesIterator.hasNext()) {
            color = typeValuesIterator.next();
            exp = new Expression_Add(
                new Expression_FunctionValue(ptPlace, color),
                exp
            );
        }


        expression.addParameter(new Expression_Compare(
            exp,
            Expression_Compare.getType(operator),
            new Expression_IntegerLiteral(value.getConstant())
        ));

    }

}


