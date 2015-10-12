package dk.aau.cs.TCTL.XMLParsing;

import java.util.ArrayList;
import java.util.Iterator;

import dk.aau.cs.TCTL.TCTLPlusListNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.TCTLTermListNode;
import dk.aau.cs.TCTL.AritmeticOperator;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class XMLQueryParser {

    private Node property;

    public static TCTLAbstractProperty parse(Node prop) throws XMLQueryParseException{
        XMLQueryParser parser = new XMLQueryParser(prop);
        return parser.AbstractProperty();
    }

    final public TCTLAbstractProperty AbstractProperty() throws XMLQueryParseException{
        Node formula = null;
        if((formula = findSubNode("formula", property)) != null){
            return parseFormula(formula);
        } else{
            throw new XMLQueryParseException("An error occurred while parsing: " + property.getNodeName());
        }
    }

    private TCTLAbstractProperty parseFormula(Node formula)
        throws XMLQueryParseException{

        TCTLAbstractStateProperty booleanFormula = null;
        Node operator = getFirstChildNode(formula);
        Node child = getFirstChildNode(operator);
        String nodeNameOperator = operator.getNodeName();
        String nodeNameChild = child.getNodeName();

        if(nodeNameOperator.equals("invariant")){
            // TODO remember to negate result!
            //return new TCTLEFNode();
            throw new XMLQueryParseException("An error occurred while parsing: " + nodeNameOperator);
        } else if(nodeNameOperator.equals("impossibility")){
            // TODO remember to negate result!
            //return new TCTLEFNode();
            throw new XMLQueryParseException("An error occurred while parsing: " + nodeNameOperator);
        } else if(nodeNameOperator.equals("possibility")){
            // TODO remember to negate result in: invariant, impossibility and possibility
            //return new TCTLEFNode();
            throw new XMLQueryParseException("An error occurred while parsing: " + nodeNameOperator);
        } else if(nodeNameOperator.equals("exists-path")){
            if (nodeNameChild.equals("finally")){
                booleanFormula = parseBooleanFormula(getFirstChildNode(child));
                return new TCTLEFNode(booleanFormula);
            } else{
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeNameOperator);
            }
        } else if(nodeNameOperator.equals("all-paths")){
            if (nodeNameChild.equals("globally")){
                booleanFormula = parseBooleanFormula(getFirstChildNode(child));
                return new TCTLAGNode(booleanFormula);
            } else{
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeNameOperator);
            }
        }
        throw new XMLQueryParseException("An error occurred while parsing: " + nodeNameOperator);
    }

    private TCTLAbstractStateProperty parseBooleanFormula(Node booleanFormula)
        throws XMLQueryParseException{

        String nodeName = booleanFormula.getNodeName();
        ArrayList<Node> children = null;

        if(nodeName.equals("deadlock")){
            return new TCTLDeadlockNode();
        } else if(nodeName.equals("true")){
            return new TCTLTrueNode();
        } else if(nodeName.equals("false")){
            return new TCTLFalseNode();
        } else if(nodeName.equals("negation")){
            if(getChildCount(booleanFormula) == 1){
                TCTLAbstractStateProperty booleanFormulaChild =
                    parseBooleanFormula(getFirstChildNode(booleanFormula));
                return new TCTLNotNode(booleanFormulaChild);
            } else{
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }
        } else if(nodeName.equals("conjunction") || nodeName.equals("disjunction")){
            if(getChildCount(booleanFormula) < 2){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            children = getAllChildren(booleanFormula);
            ArrayList<TCTLAbstractStateProperty> boolExpList = 
                new ArrayList<TCTLAbstractStateProperty>();

            for (Node n : children){
                boolExpList.add(parseBooleanFormula(n));
            }

            if(nodeName == "conjunction"){
                return new TCTLAndListNode(boolExpList);
            } else{
                return new TCTLOrListNode(boolExpList);
            }
        } else if(nodeName.equals("exclusive-disjunction")){
            children = getAllChildren(booleanFormula);

            if(children.size() != 2){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> boolExpList = 
                new ArrayList<TCTLAbstractStateProperty>();

            // Parse boolean expressions
            boolExpList.add(parseBooleanFormula(children.get(0)));
            boolExpList.add(parseBooleanFormula(children.get(1)));

            // Build left sub tree
            TCTLAndListNode leftSubFormula = 
                new TCTLAndListNode(boolExpList.get(0), new TCTLNotNode(boolExpList.get(1)));

            // Build right sub tree
            TCTLAndListNode rightSubFormula = 
                new TCTLAndListNode(new TCTLNotNode(boolExpList.get(0)), boolExpList.get(1));

            return new TCTLOrListNode(leftSubFormula, rightSubFormula);
        } else if(nodeName.equals("implication")){
            children = getAllChildren(booleanFormula);

            if(children.size() != 2){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> boolExpList = 
                new ArrayList<TCTLAbstractStateProperty>();

            // Parse boolean expressions
            boolExpList.add(parseBooleanFormula(children.get(0)));
            boolExpList.add(parseBooleanFormula(children.get(1)));

            return new TCTLOrListNode(new TCTLNotNode(boolExpList.get(0)), boolExpList.get(1));
        } else if(nodeName.equals("equivalence")){
            children = getAllChildren(booleanFormula);

            if(children.size() != 2){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> boolExpList = 
                new ArrayList<TCTLAbstractStateProperty>();

            // Parse boolean expressions
            boolExpList.add(parseBooleanFormula(children.get(0)));
            boolExpList.add(parseBooleanFormula(children.get(1)));

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

            if(getChildCount(booleanFormula) != 2){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            children = getAllChildren(booleanFormula);
            TCTLAbstractStateProperty subformula1 = parseIntegerExpression(children.get(0));
            TCTLAbstractStateProperty subformula2 = parseIntegerExpression(children.get(1));

            if(nodeName.equals("integer-eq")){
                return new TCTLAtomicPropositionNode(subformula1, "==", subformula2);
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
        }

        // We do not support "is-fireable" queries

        throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
    }

    private TCTLAbstractStateProperty parseIntegerExpression(Node integerExpression)
        throws XMLQueryParseException{

        ArrayList<Node> children = null;
        String nodeName = integerExpression.getNodeName(); 

        if(nodeName.equals("integer-constant")){
            String value = null;
            if((value = getText(integerExpression)) == null){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }
            return new TCTLConstNode(Integer.parseInt(value));
        } else if(nodeName.equals("tokens-count")){
            children = getAllChildren(integerExpression);
            if(children.size() < 1){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> places = 
                new ArrayList<TCTLAbstractStateProperty>();

            for(Node n : children){
                String placeName = getText(n);
                places.add(new TCTLPlaceNode(placeName));
            }

            return new TCTLPlusListNode(places);
        } else if(nodeName.equals("integer-sum") || nodeName.equals("integer-product")){
            children = getAllChildren(integerExpression);
            Iterator<Node> itr = children.iterator();

            if(children.size() < 2){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> intExpList = 
                new ArrayList<TCTLAbstractStateProperty>();

            while(itr.hasNext()){
                Node n = itr.next();
                intExpList.add(parseIntegerExpression(n));

                if(itr.hasNext()){
                    if(nodeName.equals("integer-sum")){
                        intExpList.add(new AritmeticOperator("+"));
                    } else{
                        intExpList.add(new AritmeticOperator("*"));
                    }
                }
            }
            
            return new TCTLTermListNode(intExpList);
        } else if(nodeName.equals("integer-difference") || nodeName.equals("integer-division")){
            children = getAllChildren(integerExpression);

            if(children.size() != 2){
                throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> intExpList = 
                new ArrayList<TCTLAbstractStateProperty>();

            intExpList.add(parseIntegerExpression(children.get(0)));

            if(nodeName.equals("integer-difference")){
                intExpList.add(new AritmeticOperator("-"));
            } else{
                intExpList.add(new AritmeticOperator("/"));
            }

            intExpList.add(parseIntegerExpression(children.get(1)));
            
            return new TCTLTermListNode(intExpList);
        }

        throw new XMLQueryParseException("An error occurred while parsing: " + nodeName);
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

    public XMLQueryParser(Node prop){
        this.property = prop;
    }
}
