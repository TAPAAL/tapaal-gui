package dk.aau.cs.TCTL.XMLParsing;

import java.util.ArrayList;

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
        /* We only support EF and AG formulas. It is therefore
         * unnecessary to check for operators F and G.
         */
        Node pathQuantifier = getFirstChildNode(formula);
        TCTLAbstractStateProperty booleanFormula = null;

        // Skip tags <finally> or <globally>
        Node child = getFirstChildNode(getFirstChildNode(pathQuantifier));
        

        switch(pathQuantifier.getNodeName()){
            case "exists-path":
                booleanFormula = parseBooleanFormula(child);
                return new TCTLEFNode(booleanFormula);

            case "all-paths":
                booleanFormula = parseBooleanFormula(child);
                return new TCTLAGNode(booleanFormula);
        }
        throw new XMLQueryParseException("No path quantifier found");
    }

    private TCTLAbstractStateProperty parseBooleanFormula(Node booleanFormula)
        throws XMLQueryParseException{
        //TODO continue from here
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
