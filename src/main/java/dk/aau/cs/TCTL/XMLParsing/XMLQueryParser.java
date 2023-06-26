package dk.aau.cs.TCTL.XMLParsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.NumberFormatException;

import dk.aau.cs.debug.Logger;
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
    private QueryWrapper queryWrapper;
    private static final String ERROR_MESSAGE = "Could not parse XML tag: ";
    
    public static boolean parse(Node prop, QueryWrapper queryWrapper){

        XMLQueryParser parser = new XMLQueryParser(prop, queryWrapper);
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
    	XMLQueryParser parser = new XMLQueryParser(prop);
    	
        return parser.AbstractProperty();
    }

    final public TCTLAbstractProperty AbstractProperty() throws XMLQueryParseException{
        Node formula;

        if((formula = findSubNode("formula", property)) != null){
            return parseFormula(formula);
        } else{
            throw new XMLQueryParseException(ERROR_MESSAGE + property.getNodeName());
        }
    }

    private String parsePropertyName(){
        Node idNode;
        String result;

        // Find <id> tag and get property name
        if(((idNode = XMLQueryParser.findSubNode("id", this.property)) == null) ||
            ((result = XMLQueryParser.getText(idNode)) == null)){

            // If no name was found, set generic name
            result = "Query Comment/Name Here";
        }

        return result;
    }

    private TCTLAbstractProperty parseFormula(Node formula)
        throws XMLQueryParseException{

        TCTLAbstractStateProperty booleanFormula;
        Node operator = getFirstChildNode(formula);
        Node child = getFirstChildNode(operator);
        String nodeName = operator.getNodeName();
        String childNodeName = child.getNodeName();

        if(nodeName.equals("invariant")){
            booleanFormula = parseBooleanFormula(getFirstChildNode(operator));
            this.queryWrapper.negateQuery();
            return new TCTLEFNode(new TCTLNotNode(booleanFormula));
        } else if(nodeName.equals("impossibility")){
            booleanFormula = parseBooleanFormula(getFirstChildNode(operator));
            this.queryWrapper.negateQuery();
            return new TCTLEFNode(booleanFormula);
        } else if(nodeName.equals("possibility")){
            booleanFormula = parseBooleanFormula(getFirstChildNode(operator));
            return new TCTLEFNode(booleanFormula);
        } else if(nodeName.equals("exists-path")){
            if (childNodeName.equals("finally")){
                booleanFormula = parseBooleanFormula(getFirstChildNode(child));
                return new TCTLEFNode(booleanFormula);
            } else{
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }
        } else if(nodeName.equals("all-paths")){
            if (childNodeName.equals("globally")){
                booleanFormula = parseBooleanFormula(getFirstChildNode(child));
                return new TCTLAGNode(booleanFormula);
            } else{
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }
        }
        throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
    }

    private TCTLAbstractStateProperty parseBooleanFormula(Node booleanFormula)
        throws XMLQueryParseException{

        String nodeName = booleanFormula.getNodeName();
        ArrayList<Node> children;

        if(nodeName.equals("deadlock")){
            return new TCTLDeadlockNode();
        } else if(nodeName.equals("true")){
            return new TCTLTrueNode();
        } else if(nodeName.equals("false")){
            return new TCTLFalseNode();
        } else if(nodeName.equals("negation")){
            children = getAllChildren(booleanFormula);

            if(children.size() != 1){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            TCTLAbstractStateProperty booleanFormulaChild =
                parseBooleanFormula(getFirstChildNode(booleanFormula));

            return new TCTLNotNode(booleanFormulaChild);
        } else if(nodeName.equals("conjunction") || nodeName.equals("disjunction")){
            children = getAllChildren(booleanFormula);

            if(children.size() < 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

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
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
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
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
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
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
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

            children = getAllChildren(booleanFormula);

            if(children.size() != 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

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
                    } else{
                        intExpList.add(new AritmeticOperator("*"));
                    }
                }
            }
            
            return new TCTLTermListNode(intExpList);
        } else if(nodeName.equals("integer-difference")){
            children = getAllChildren(integerExpression);

            if(children.size() != 2){
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> intExpList = 
                new ArrayList<TCTLAbstractStateProperty>();

            intExpList.add(parseIntegerExpression(children.get(0)));
            intExpList.add(new AritmeticOperator("-"));
            intExpList.add(parseIntegerExpression(children.get(1)));
            
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

    public XMLQueryParser(Node prop, QueryWrapper qw){
        this.property = prop;
        this.queryWrapper = qw;
    }
    
    public XMLQueryParser(Node prop){
    	this.property = prop;
    }
}
