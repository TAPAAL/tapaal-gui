package dk.aau.cs.TCTL.XMLParsing;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.AritmeticOperator;
import dk.aau.cs.TCTL.TCTLPlusListNode;
import dk.aau.cs.TCTL.TCTLTermListNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class XMLQueryParser {

    private Node property;

    public static TCTLAbstractProperty parse(Node prop) throws XMLQueryParseException{
        XMLQueryParser parser = new XMLQueryParser(prop);
        return parser.AbstractProperty();
    }

    final public TCTLAbstractProperty AbstractProperty() throws XMLQueryParseException{
        Node formula = findSubNode("formula", property);
        return parseFormula(formula);
    }

    private TCTLAbstractProperty parseFormula(Node formula)
        throws XMLQueryParseException{

        TCTLAbstractStateProperty booleanFormula = null;
        Node operator = getFirstChildNode(formula);
        Node child = getFirstChildNode(operator);
        String pathOperator = null;

        switch(operator.getNodeName()){
            /* TODO create new reachability operators for the MCC14 queries
            // MCC14 Reachability operators
            case "invariant":
                booleanFormula = parseBooleanFormula(child);
                return new TCTLInvariantNode(booleanFormula);

            case "impossibility":
                booleanFormula = parseBooleanFormula(child);
                return new TCTLImpossibilityNode(booleanFormula);

            case "possibility":
                booleanFormula = parseBooleanFormula(child);
                return new TCTLPossibilityNode(booleanFormula);*/

            // State operators
            case "exists-path":
                pathOperator = getFirstChildNode(child).getNodeName();
                if (pathOperator.equals("finally")){
                    booleanFormula = parseBooleanFormula(getFirstChildNode(child));
                    return new TCTLEFNode(booleanFormula);
                } else{
                    throw new XMLQueryParseException("Invalid path operator: " + pathOperator);
                }

            case "all-paths":
                pathOperator = getFirstChildNode(child).getNodeName();
                if (pathOperator.equals("globally")){
                    booleanFormula = parseBooleanFormula(getFirstChildNode(child));
                    return new TCTLAGNode(booleanFormula);
                } else{
                    throw new XMLQueryParseException("Invalid path operator: " + pathOperator);
                }
        }
        throw new XMLQueryParseException("No state operator found");
    }

    private TCTLAbstractStateProperty parseBooleanFormula(Node booleanFormula)
        throws XMLQueryParseException{

        String nodeName = booleanFormula.getNodeName();

        switch(nodeName){
            case "deadlock":
                return new TCTLDeadlockNode();

            case "true":
                return new TCTLTrueNode();

            case "false":
                return new TCTLFalseNode();

            case "negation":
                if(getChildCount(booleanFormula) == 1){
                    TCTLAbstractStateProperty booleanFormulaChild =
                        parseBooleanFormula(getFirstChildNode(booleanFormula));
                    return new TCTLNotNode(booleanFormulaChild);
                } else{
                    throw new XMLQueryParseException("Invalid negation");
                }

            case "conjunction":
            case "disjunction":
                if(getChildCount(booleanFormula) < 2){
                    throw new XMLQueryParseException("Invalid use of: " 
                        + nodeName);
                }

                ArrayList<Node> children = getAllChildren(booleanFormula);
                ArrayList<TCTLAbstractStateProperty> properties = 
                    new ArrayList<TCTLAbstractStateProperty>();

                for (Node n : children){
                    properties.add(parseBooleanFormula(n));
                }

                if(nodeName == "conjunction"){
                    return new TCTLAndListNode(properties);
                } else{
                    return new TCTLOrListNode(properties);
                }

            case "exclusive-disjunction":
            case "implication":
            case "equivalence":

            case "integer-eq":
            case "integer-ne":
            case "integer-lt":
            case "integer-le":
            case "integer-gt":
            case "integer-ge":

            case "is-fireable":

        }
    }

    private TCTLConstNode parseIntegerExpression(Node integerExpression){
        switch (integerExpression.getNodeName()){
            case "integer-constant":
            case "tokens-count":

            case "integer-sum":
            case "integer-product":

            case "integer-difference":
        }
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

    public String getText(Node node){
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
