package dk.aau.cs.TCTL.XMLParsing;

import dk.aau.cs.TCTL.TCTLPlaceNode;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

final class XMLQueryParserUtils {
    private XMLQueryParserUtils() {}

    static TCTLPlaceNode parsePlaceTokensCount(List<Node> children, String error) throws XMLQueryParseException {
        if (children.isEmpty()) throw new XMLQueryParseException(error);
        var placeNode = children.get(0);
        if (!"place".equals(placeNode.getNodeName())) throw new XMLQueryParseException(error);

        var placeChildren = elementChildren(placeNode);
        Node colorExpression = null;
        if (placeChildren.size() == 1 && "color-expression".equals(placeChildren.get(0).getNodeName())) {
            colorExpression = placeChildren.get(0);
        } else if (!placeChildren.isEmpty()) {
            throw new XMLQueryParseException(error);
        }

        if (children.size() > 1) {
            if (children.stream().anyMatch(child -> "color-expression".equals(child.getNodeName()))) {
                throw new XMLQueryParseException(error);
            }
            return null;
        }

        var place = directText(placeNode).split("\\.", -1);
        if (place.length > 2 || place[0].isEmpty() || place[place.length - 1].isEmpty()) {
            throw new XMLQueryParseException(error);
        }

        var color = colorExpression == null ? null : parseColor(colorExpression, error);
        return place.length == 2
            ? new TCTLPlaceNode(place[0], place[1], color)
            : new TCTLPlaceNode("", place[0], color);
    }

    private static String parseColor(Node expression, String error) throws XMLQueryParseException {
        var children = elementChildren(expression);
        if (children.size() != 1 || !"color".equals(children.get(0).getNodeName())) {
            throw new XMLQueryParseException(error);
        }

        var color = children.get(0).getTextContent().trim();
        if (color.isEmpty()) throw new XMLQueryParseException(error);
        return color;
    }

    private static String directText(Node parent) {
        var text = new StringBuilder();
        var children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            var child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(child.getNodeValue());
            }
        }

        return text.toString().trim();
    }

    private static List<Node> elementChildren(Node parent) {
        var children = parent.getChildNodes();
        var elements = new ArrayList<Node>();
        for (int i = 0; i < children.getLength(); ++i) {
            var child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) elements.add(child);
        }
        return elements;
    }
}
