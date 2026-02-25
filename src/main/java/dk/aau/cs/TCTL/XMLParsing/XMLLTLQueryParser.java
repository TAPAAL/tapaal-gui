package dk.aau.cs.TCTL.XMLParsing;

import java.util.ArrayList;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.debug.Logger;
import org.w3c.dom.Node;

public class XMLLTLQueryParser extends AbstractXMLQueryParser {

    public static boolean parse(Node prop, QueryWrapper queryWrapper){

        XMLLTLQueryParser parser = new XMLLTLQueryParser(prop, queryWrapper);
        queryWrapper.setName(parser.parsePropertyName());

        try{
            queryWrapper.setProp(parser.AbstractProperty());
        } catch (XMLQueryParseException e){
            queryWrapper.setException(e);
            Logger.log(e);
            return false;
        }

        return true;
    }

    public static TCTLAbstractProperty parse(Node prop) throws XMLQueryParseException{
        XMLLTLQueryParser parser = new XMLLTLQueryParser(prop);

        return parser.AbstractProperty();
    }

    final public TCTLAbstractProperty AbstractProperty() throws XMLQueryParseException{
        Node formula;

        if((formula = findSubNode("formula", property)) != null){
            return parseFormula(getFirstChildNode(formula));
        } else{
            throw new XMLQueryParseException(ERROR_MESSAGE + property.getNodeName());
        }
    }

    private String parsePropertyName(){
        Node idNode;
        String result;

        // Find <id> tag and get property name
        if (((idNode = AbstractXMLQueryParser.findSubNode("id", this.property)) == null) ||
            ((result = AbstractXMLQueryParser.getText(idNode)) == null)) {

            // If no name was found, set generic name
            result = "Query Comment/Name Here";
        }

        return result;
    }

    private TCTLAbstractProperty parseFormula(Node property)
        throws XMLQueryParseException{

        TCTLAbstractProperty childProperty;
        Node child = getFirstChildNode(property);
        String nodeName = property.getNodeName();

        ArrayList<Node> children;

        if(nodeName.equals("invariant")){
            childProperty = parseFormula(getFirstChildNode(property));
            this.queryWrapper.negateQuery();
            if(childProperty instanceof TCTLAbstractPathProperty){
                childProperty = new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty);
            }
            return new TCTLEFNode(new TCTLNotNode((TCTLAbstractStateProperty)childProperty));
        } else if(nodeName.equals("impossibility")){
            childProperty = parseFormula(getFirstChildNode(property));
            this.queryWrapper.negateQuery();
            if(childProperty instanceof TCTLAbstractPathProperty){
                childProperty = new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty);
            }
            return new TCTLEFNode((TCTLAbstractStateProperty)childProperty);
        } else if(nodeName.equals("possibility")){
            childProperty = parseFormula(getFirstChildNode(property));
            if(childProperty instanceof TCTLAbstractPathProperty){
                childProperty = new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty);
            }
            return new TCTLEFNode((TCTLAbstractStateProperty)childProperty);
        } else if(nodeName.equals("all-paths")){
            childProperty = parseFormula(child);
            if(childProperty instanceof TCTLAbstractPathProperty){
                return new LTLANode(new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty));
            } else{
                return new LTLANode((TCTLAbstractStateProperty)childProperty);
            }
        }  else if(nodeName.equals("exists-path")){
            childProperty = parseFormula(child);
            if(childProperty instanceof TCTLAbstractPathProperty){
                return new LTLENode(new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty));
            } else{
                return new LTLENode((TCTLAbstractStateProperty)childProperty);
            }
        } else if (nodeName.equals("finally")){
            childProperty = parseFormula(child);
            if(childProperty instanceof TCTLAbstractPathProperty){
                return new LTLFNode(new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty));
            } else{
                return new LTLFNode((TCTLAbstractStateProperty)childProperty);
            }
        } else if (nodeName.equals("globally")){
            childProperty = parseFormula(child);
            if(childProperty instanceof TCTLAbstractPathProperty){
                return new LTLGNode(new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty));
            } else{
                return new LTLGNode((TCTLAbstractStateProperty)childProperty);
            }
        } else if (nodeName.equals("next")){
            childProperty = parseFormula(child);
            if(childProperty instanceof TCTLAbstractPathProperty){
                return new LTLXNode(new TCTLPathToStateConverter((TCTLAbstractPathProperty)childProperty));
            } else{
                return new LTLXNode((TCTLAbstractStateProperty)childProperty);
            }
        } else if (nodeName.equals("until")){
            children = getAllChildren(property);
            if(children.size() != 2){
                Logger.log("ERROR");
            }

            TCTLAbstractProperty before = parseFormula(getFirstChildNode(children.get(0)));
            TCTLAbstractProperty reach = parseFormula(getFirstChildNode(children.get(1)));

            if(before instanceof TCTLAbstractPathProperty){
                before = new TCTLPathToStateConverter((TCTLAbstractPathProperty)before);
            }
            if(reach instanceof TCTLAbstractPathProperty){
                reach = new TCTLPathToStateConverter((TCTLAbstractPathProperty)reach);
            }

            return new LTLUNode((TCTLAbstractStateProperty)before, (TCTLAbstractStateProperty)reach);
        } else if(nodeName.equals("true")){
            return new TCTLTrueNode();
        } else if(nodeName.equals("false")){
            return new TCTLFalseNode();
        } else if(nodeName.equals("negation")){
            children = getAllChildren(property);

            if(children.size() != 1){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            TCTLAbstractProperty booleanFormulaChild =
                parseFormula(getFirstChildNode(property));
            if(booleanFormulaChild instanceof TCTLAbstractPathProperty){
                booleanFormulaChild = new TCTLPathToStateConverter((TCTLAbstractPathProperty)booleanFormulaChild);
            }
            return new TCTLNotNode((TCTLAbstractStateProperty)booleanFormulaChild);
        } else if(nodeName.equals("conjunction") || nodeName.equals("disjunction")){
            children = getAllChildren(property);

            if(children.size() < 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> boolExpList =
                new ArrayList<TCTLAbstractStateProperty>();

            for (Node n : children){
                TCTLAbstractProperty p = parseFormula(n);
                if(p instanceof TCTLAbstractPathProperty){
                    p = new TCTLPathToStateConverter((TCTLAbstractPathProperty)p);
                }
                boolExpList.add((TCTLAbstractStateProperty)p);
            }

            if(nodeName == "conjunction"){
                return new TCTLAndListNode(boolExpList);
            } else{
                return new TCTLOrListNode(boolExpList);
            }
        } else if(nodeName.equals("exclusive-disjunction")){
            children = getAllChildren(property);

            if(children.size() != 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> boolExpList =
                new ArrayList<TCTLAbstractStateProperty>();

            TCTLAbstractProperty left = parseFormula(children.get(0));
            TCTLAbstractProperty right = parseFormula(children.get(1));

            if(left instanceof TCTLAbstractPathProperty){
                left = new TCTLPathToStateConverter((TCTLAbstractPathProperty)left);
            }
            if(right instanceof TCTLAbstractPathProperty){
                right = new TCTLPathToStateConverter((TCTLAbstractPathProperty)right);
            }

            // Parse boolean expressions
            boolExpList.add((TCTLAbstractStateProperty)left);
            boolExpList.add((TCTLAbstractStateProperty)right);

            // Build left sub tree
            TCTLAndListNode leftSubFormula =
                new TCTLAndListNode(boolExpList.get(0), new TCTLNotNode(boolExpList.get(1)));

            // Build right sub tree
            TCTLAndListNode rightSubFormula =
                new TCTLAndListNode(new TCTLNotNode(boolExpList.get(0)), boolExpList.get(1));

            return new TCTLOrListNode(leftSubFormula, rightSubFormula);

        } else if(nodeName.equals("implication")){
            children = getAllChildren(property);

            if(children.size() != 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> boolExpList =
                new ArrayList<TCTLAbstractStateProperty>();

            TCTLAbstractProperty left = parseFormula(children.get(0));
            TCTLAbstractProperty right = parseFormula(children.get(1));

            if(left instanceof TCTLAbstractPathProperty){
                left = new TCTLPathToStateConverter((TCTLAbstractPathProperty)left);
            }
            if(right instanceof TCTLAbstractPathProperty){
                right = new TCTLPathToStateConverter((TCTLAbstractPathProperty)right);
            }

            // Parse boolean expressions
            boolExpList.add((TCTLAbstractStateProperty)left);
            boolExpList.add((TCTLAbstractStateProperty)right);

            return new TCTLOrListNode(new TCTLNotNode(boolExpList.get(0)), boolExpList.get(1));
        } else if(nodeName.equals("equivalence")){
            children = getAllChildren(property);

            if(children.size() != 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> boolExpList =
                new ArrayList<TCTLAbstractStateProperty>();

            TCTLAbstractProperty left = parseFormula(children.get(0));
            TCTLAbstractProperty right = parseFormula(children.get(1));

            if(left instanceof TCTLAbstractPathProperty){
                left = new TCTLPathToStateConverter((TCTLAbstractPathProperty)left);
            }
            if(right instanceof TCTLAbstractPathProperty){
                right = new TCTLPathToStateConverter((TCTLAbstractPathProperty)right);
            }

            // Parse boolean expressions
            boolExpList.add((TCTLAbstractStateProperty)left);
            boolExpList.add((TCTLAbstractStateProperty)right);

            // Build left sub tree
            TCTLAndListNode leftSubFormula =
                new TCTLAndListNode(boolExpList.get(0), boolExpList.get(1));

            // Build right sub tree
            TCTLAndListNode rightSubFormula =
                new TCTLAndListNode(new TCTLNotNode(boolExpList.get(0)), new TCTLNotNode(boolExpList.get(1)));

            return new TCTLOrListNode(leftSubFormula, rightSubFormula);
        } else if(nodeName.equals("integer-eq") || nodeName.equals("integer-ne") ||
            nodeName.equals("integer-lt") || nodeName.equals("integer-le") ||
            nodeName.equals("integer-gt") || nodeName.equals("integer-ge")){

            children = getAllChildren(property);

            if(children.size() != 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            TCTLAbstractStateProperty subformula1 = parseIntegerExpression(children.get(0));
            TCTLAbstractStateProperty subformula2 = parseIntegerExpression(children.get(1));

            if(nodeName.equals("integer-eq")){
                return new TCTLAtomicPropositionNode(subformula1, "=", subformula2);
            } else if(nodeName.equals("integer-ne")){
                return new TCTLAtomicPropositionNode(subformula1, "!=", subformula2);
            } else if(nodeName.equals("integer-lt")){
                return new TCTLAtomicPropositionNode(subformula1, "<", subformula2);
            } else if(nodeName.equals("integer-le")){
                return new TCTLAtomicPropositionNode(subformula1, "<=", subformula2);
            } else if(nodeName.equals("integer-gt")){
                return new TCTLAtomicPropositionNode(subformula1, ">", subformula2);
            } else if(nodeName.equals("integer-ge")){
                return new TCTLAtomicPropositionNode(subformula1, ">=", subformula2);
            }
        } else if (nodeName.equals("is-fireable")){
            // Construct a nested disjunction of transitions.

            children = getAllChildren(property);

            if(children.isEmpty()){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            } else if(children.size() == 1) {
                String[] splits = getText(children.get(0)).replace("\n", "").split("\\.");

                // Check if transition contains a template name
                if(splits.length > 1){
                    return new TCTLTransitionNode(splits[0], splits[1]);
                } else {
                    return new TCTLTransitionNode(splits[0]);
                }
            } else {
                ArrayList<TCTLAbstractStateProperty> transitions = new ArrayList<TCTLAbstractStateProperty>();

                for(Node n : children) {
                    String[] splits = getText(n).replace("\n", "").split("\\.");

                    // Check if transition contains a template name
                    if(splits.length > 1){
                        transitions.add(new TCTLTransitionNode(splits[0], splits[1]));
                    } else {
                        transitions.add(new TCTLTransitionNode(splits[0]));
                    }
                }
                return new TCTLOrListNode(transitions);
            }
        } else if(nodeName.equals("deadlock")) {
            return new TCTLDeadlockNode();
        } else {
            parseFormula(property);
        }

        throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
    }

    public XMLLTLQueryParser(Node prop, QueryWrapper qw){
        super(prop, qw);
    }

    public XMLLTLQueryParser(Node prop){
        super(prop);
    }
}
