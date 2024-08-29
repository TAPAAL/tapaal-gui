package dk.aau.cs.pddl;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedPlace;
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

    private IExpression_Value parsePlace(TCTLPlaceNode node) {
        var ptPlace = this.petriNet.getPlaceByName(node.getPlace());

        return makePlaceSum(ptPlace);
    }

    private IExpression_Value makePlaceSum(TimedPlace ptPlace) {
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

        return exp;
    }

    private IExpression_Value parseConst(TCTLConstNode node) {
        return new Expression_IntegerLiteral(node.getConstant());
    }

    private void parseProperty(TCTLAtomicPropositionNode property) throws UnhandledExpressionType {
        TCTLAbstractStateProperty leftAbstract = property.getLeft();
        TCTLAbstractStateProperty rightAbstract = property.getRight();

        IExpression_Value left;
        String operator = property.getOp();
        IExpression_Value right;

        if(leftAbstract.getClass() == TCTLPlaceNode.class)
            left = parsePlace((TCTLPlaceNode)leftAbstract);
        else if(leftAbstract.getClass() == TCTLConstNode.class)
            left = parseConst((TCTLConstNode)leftAbstract);
        else
            throw new UnhandledExpressionType();

        if(rightAbstract.getClass() == TCTLPlaceNode.class)
            right = parsePlace((TCTLPlaceNode)rightAbstract);
        else if(rightAbstract.getClass() == TCTLConstNode.class)
            right = parseConst((TCTLConstNode)rightAbstract);
        else
            throw new UnhandledExpressionType();


        expression.addParameter(Expression_Compare.Make_Comparison(
            left,
            Expression_Compare.getType(operator),
            right
        ));

    }

}


