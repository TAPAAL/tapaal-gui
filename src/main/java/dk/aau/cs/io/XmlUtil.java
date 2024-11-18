package dk.aau.cs.io;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlUtil {
    public static boolean isDescendantOfTag(Node node, String tagName) {
        Node parent = node.getParentNode();
        while (parent != null) {
            if (parent.getNodeType() == Node.ELEMENT_NODE &&((Element) parent).getTagName().equals(tagName)) {
                return true;
            }

            parent = parent.getParentNode();
        }
        
        return false;
    }
}
