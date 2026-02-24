package dk.aau.cs.TCTL.XMLParsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.NumberFormatException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.aau.cs.TCTL.AritmeticOperator;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLTermListNode;

public abstract class AbstractXMLQueryParser {
    protected Node property;
    protected QueryWrapper queryWrapper;
    protected static final String ERROR_MESSAGE = "Could not parse XML tag: ";

    public AbstractXMLQueryParser(Node prop, QueryWrapper qw) {
        this.property = prop;
        this.queryWrapper = qw;
    }

    public AbstractXMLQueryParser(Node prop) {
        this.property = prop;
    }

    protected TCTLAbstractStateProperty parseIntegerExpression(Node integerExpression) throws XMLQueryParseException {
        ArrayList<Node> children;
        String nodeName = integerExpression.getNodeName();

        if (nodeName.equals("integer-constant")) {
            String value;
            int result;

            if ((value = getText(integerExpression)) == null) {
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            value = value.replace("\n", "");

            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            return new TCTLConstNode(result);
        } else if (nodeName.equals("tokens-count")) {
            children = getAllChildren(integerExpression);

            if (children.size() < 1) {
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> terms = new ArrayList<TCTLAbstractStateProperty>();
            Iterator<Node> itr = children.iterator();

            while (itr.hasNext()) {
                Node n = itr.next();
                if (n instanceof Element && ((Element) n).hasAttribute("id")) {
                    Element place = (Element) n;
                    String id = place.getAttribute("id");
                    String component = place.hasAttribute("component") ? place.getAttribute("component") : "";
                    terms.add(new TCTLPlaceNode(component, id));
                } else {
                    String[] splits = getText(n).replace("\n", "").split("\\.");
                    // Check if place contains a template name
                    if (splits.length > 1) {
                        terms.add(new TCTLPlaceNode(splits[0], splits[1]));
                    } else {
                        terms.add(new TCTLPlaceNode(splits[0]));
                    }
                }

                if (itr.hasNext()) {
                    terms.add(new AritmeticOperator("+"));
                }
            }

            if (terms.size() == 1) {
                return terms.get(0);
            } else {
                return new TCTLTermListNode(terms);
            }
        } else if (nodeName.equals("integer-sum") || nodeName.equals("integer-product")
                || nodeName.equals("integer-difference")) {

            children = getAllChildren(integerExpression);
            Iterator<Node> itr = children.iterator();

            if (children.size() < 2) {
                throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
            }

            ArrayList<TCTLAbstractStateProperty> intExpList = new ArrayList<TCTLAbstractStateProperty>();

            while (itr.hasNext()) {
                Node n = itr.next();
                intExpList.add(parseIntegerExpression(n));

                if (itr.hasNext()) {
                    if (nodeName.equals("integer-sum")) {
                        intExpList.add(new AritmeticOperator("+"));
                    } else if (nodeName.equals("integer-product")) {
                        intExpList.add(new AritmeticOperator("*"));
                    } else {
                        intExpList.add(new AritmeticOperator("-"));
                    }
                }
            }

            return new TCTLTermListNode(intExpList);
        }

        throw new XMLQueryParseException(ERROR_MESSAGE + nodeName);
    }

    public static Node findSubNode(String name, Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        if (!node.hasChildNodes()) {
            return null;
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node subNode = children.item(i);
            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                if (subNode.getNodeName().equals(name))
                    return subNode;
            }
        }

        return null;
    }

    protected ArrayList<Node> getAllChildren(Node parentNode) {
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

    public static String getText(Node node) {
        String result = null;

        if (!node.hasChildNodes()) {
            return null;
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node subNode = children.item(i);
            if (subNode.getNodeType() == Node.TEXT_NODE) {
                result = subNode.getNodeValue();
            }
        }

        return result;
    }

    public int getChildCount(Node node) {
        int result = 0;

        if (!node.hasChildNodes()) {
            return result;
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node subNode = children.item(i);
            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                result++;
            }
        }

        return result;
    }

    protected Node getFirstChildNode(Node parent) {
        Node child = parent.getFirstChild();

        while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
            child = child.getNextSibling();
        }

        return child;
    }
}
