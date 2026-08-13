package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;

final class XMLQueryVisitorUtils {
    private XMLQueryVisitorUtils() {}

    static String tokensCount(TCTLPlaceNode placeNode) {
        var place = (placeNode.getTemplate().isEmpty() ? "" : placeNode.getTemplate() + ".") + placeNode.getPlace();
        var color = placeNode.getColor() == null ? "" : tag(tag(placeNode.getColor(), "color"), "color-expression");
        return tag(tag(place + color, "place"), "tokens-count");
    }

    private static String tag(String content, String tag) {
        return '<' + tag + '>' + content + "</" + tag + '>';
    }
}
