package dk.aau.cs.TCTL.XMLParsing;

import dk.aau.cs.TCTL.TCTLPlaceNode;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

final class XMLQueryParserUtils {
    private XMLQueryParserUtils() {}

    static TCTLPlaceNode parsePlaceTokensCount(List<Node> children, String error) throws XMLQueryParseException {
        if (children.isEmpty()) throw new XMLQueryParseException(error);
        var colored = children.size() == 2 && "color-expression".equals(children.get(1).getNodeName());
        if (children.size() > 1 && !colored) return null;
        if (!"place".equals(children.get(0).getNodeName())) throw new XMLQueryParseException(error);

        var place = children.get(0).getTextContent().trim().split("\\.");
        if (place.length > 2) throw new XMLQueryParseException(error);
        var color = colored ? parseColor(children.get(1), error) : null;
        return place.length == 2
            ? new TCTLPlaceNode(place[0], place[1], color)
            : new TCTLPlaceNode("", place[0], color);
    }

    private static String parseColor(Node expression, String error) throws XMLQueryParseException {
        var children = elementChildren(expression);
        if (children.size() != 1 || !"color".equals(children.get(0).getNodeName())) {
            throw new XMLQueryParseException(error);
        }
        return children.get(0).getTextContent().trim();
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
