package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;

import java.util.ArrayList;
import java.util.List;

final class XMLQueryVisitorUtils {
    private XMLQueryVisitorUtils() {}

    static String tokensCount(TCTLPlaceNode placeNode) {
        var place = (placeNode.getTemplate().isEmpty() ? "" : placeNode.getTemplate() + ".") + placeNode.getPlace();
        var color = placeNode.getColor() == null ? "" : tag(colorToXML(placeNode.getColor()), "color-expression");
        return tag(tag(place + color, "place"), "tokens-count");
    }

    static String colorToXML(String color) {
        if (color == null || color.isBlank()) return "";
        color = color.trim();
        if (color.startsWith("(") && color.endsWith(")")) {
            var content = new StringBuilder();
            for (var elem : splitTuple(color.substring(1, color.length() - 1))) {
                content.append(colorToXML(elem));
            }
            
            return tag(content.toString(), "tuple");
        }

        return "<color id=\"" + color + "\"/>";
    }

    private static List<String> splitTuple(String s) {
        var parts = new ArrayList<String>();
        int depth = 0, start = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == '(') ++depth;
            else if (s.charAt(i) == ')') --depth;
            else if (s.charAt(i) == ',' && depth == 0) {
                parts.add(s.substring(start, i));
                start = i + 1;
            }
        }
        
        parts.add(s.substring(start));
        return parts;
    }

    private static String tag(String content, String tag) {
        return '<' + tag + '>' + content + "</" + tag + '>';
    }
}
