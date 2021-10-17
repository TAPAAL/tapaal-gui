package dk.aau.cs.TCTL.XMLParsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.NumberFormatException;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.debug.Logger;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class XMLLTLQueryParser {

    private Node property;
    private QueryWrapper queryWrapper;
    private static final String ERROR_MESSAGE = "Could not parse XML tag: ";

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
        if(((idNode = XMLLTLQueryParser.findSubNode("id", this.property)) == null) ||
            ((result = XMLLTLQueryParser.getText(idNode)) == null)){

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
        } else{
            parseFormula(property);
        }

        throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
    }

    private TCTLAbstractStateProperty parseIntegerExpression(Node integerExpression)
        throws XMLQueryParseException{

        ArrayList<Node> children;
        String nodeName = integerExpression.getNodeName();

        if(nodeName.equals("integer-constant")){
            String value;
            int result;

            if((value = getText(integerExpression)) == null){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            value = value.replace("\n", "");

            try{
                result = Integer.parseInt(value);
            } catch (NumberFormatException e){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            return new TCTLConstNode(result);
        } else if(nodeName.equals("tokens-count")){
            children = getAllChildren(integerExpression);

            if(children.size() < 1){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            } else if (children.size() == 1) {
                String[] splits = getText(children.get(0)).replace("\n", "").split("\\.");
                // Check if place contains a template name
                if(splits.length > 1){
                    return new TCTLPlaceNode(splits[0], splits[1]);
                } else {
                    return new TCTLPlaceNode(splits[0]);
                }
            }

            ArrayList<TCTLAbstractStateProperty> terms = new ArrayList<TCTLAbstractStateProperty>();
            Iterator<Node> itr = children.iterator();

            while(itr.hasNext()){
                Node n = itr.next();
                String[] splits = getText(n).replace("\n", "").split("\\.");

                // Check if place contains a template name
                if(splits.length > 1){
                    terms.add(new TCTLPlaceNode(splits[0], splits[1]));
                } else {
                    terms.add(new TCTLPlaceNode(splits[0]));
                }

                if(itr.hasNext()){
                    terms.add(new AritmeticOperator("+"));
                }
            }

            return new TCTLTermListNode(terms);
        } else if(nodeName.equals("integer-sum") || nodeName.equals("integer-product") || nodeName.equals("integer-difference")){

            children = getAllChildren(integerExpression);
            Iterator<Node> itr = children.iterator();

            if(children.size() < 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> intExpList =
                new ArrayList<TCTLAbstractStateProperty>();

            while(itr.hasNext()){
                Node n = itr.next();
                intExpList.add(parseIntegerExpression(n));

                if(itr.hasNext()){
                    if(nodeName.equals("integer-sum")){
                        intExpList.add(new AritmeticOperator("+"));
                    } else if(nodeName.equals("integer-product")){
                        intExpList.add(new AritmeticOperator("*"));
                    } else{
                        intExpList.add(new AritmeticOperator("-"));
                    }
                }
            }

            return new TCTLTermListNode(intExpList);
        }

        throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
    }

    public static Node findSubNode(String name, Node node){
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        if (!node.hasChildNodes()){
            return null;
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++){
            Node subNode = children.item(i);
            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                if (subNode.getNodeName().equals(name))
                    return subNode;
            }
        }

        return null;
    }

    private ArrayList<Node> getAllChildren(Node parentNode) {
        NodeList children = parentNode.getChildNodes();
        ArrayList<Node> elementNodes = new ArrayList<Node>();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elementNodes.add(node);
            }
        }

        return elementNodes;
    }

    public static String getText(Node node){
        String result = null;

        if (!node.hasChildNodes()){
            return null;
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++){
            Node subNode = children.item(i);
            if (subNode.getNodeType() == Node.TEXT_NODE) {
                result = subNode.getNodeValue();
            }
        }

        return result;
    }

    public int getChildCount(Node node){
        int result = 0;

        if (!node.hasChildNodes()){
            return result;
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++){
            Node subNode = children.item(i);
            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                result++;
            }
        }

        return result;
    }

    private Node getFirstChildNode(Node parent){
        Node child = parent.getFirstChild();

        while (child != null && child.getNodeType() != Node.ELEMENT_NODE){
            child = child.getNextSibling();
        }

        return child;
    }

    public XMLLTLQueryParser(Node prop, QueryWrapper qw){
        this.property = prop;
        this.queryWrapper = qw;
    }

    public XMLLTLQueryParser(Node prop){
        this.property = prop;
    }
}
