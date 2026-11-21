package net.tapaal.gui;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Compares two saved documents while ignoring XML syntax that carries no meaning in a .tapn file.
 *
 * <p>Ignored: indentation and other whitespace-only text between elements, attribute order and
 * quoting, the XML declaration, comments, CDATA wrapping and character escaping. Everything else
 * is compared exactly, including element order, attribute values and non-whitespace text.
 */
public final class TapnXmlComparison {
    private static final int MAX_DIFFERENCES = 50;

    private final List<String> differences = new ArrayList<>();

    private TapnXmlComparison() { }

    /** Returns one line per difference, each naming the path of the element or attribute. */
    public static List<String> differences(Path expected, Path actual) throws Exception {
        try (var expectedStream = Files.newInputStream(expected); var actualStream = Files.newInputStream(actual)) {
            return differences(expectedStream, actualStream);
        }
    }

    public static List<String> differences(InputStream expected, InputStream actual) throws Exception {
        var comparison = new TapnXmlComparison();
        Element expectedRoot = parse(expected).getDocumentElement();
        Element actualRoot = parse(actual).getDocumentElement();
        comparison.compareElements(expectedRoot, actualRoot, "/" + expectedRoot.getTagName());
        return comparison.differences;
    }

    private static Document parse(InputStream input) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setCoalescing(true);
        factory.setIgnoringComments(true);
        Document document = factory.newDocumentBuilder().parse(input);
        document.normalize();
        return document;
    }

    private void compareElements(Element expected, Element actual, String path) {
        if (!expected.getTagName().equals(actual.getTagName())) {
            report(path, "expected element <" + expected.getTagName() + "> but was <" + actual.getTagName() + ">");
            return;
        }
        compareAttributes(expected, actual, path);

        List<Node> expectedChildren = significantChildren(expected);
        List<Node> actualChildren = significantChildren(actual);
        int[][] common = commonSubsequenceLengths(expectedChildren, actualChildren);
        var missing = new ArrayList<Node>();
        var unexpected = new ArrayList<Node>();
        int e = 0;
        int a = 0;
        while (e < expectedChildren.size() || a < actualChildren.size()) {
            if (e < expectedChildren.size() && a < actualChildren.size()
                && key(expectedChildren.get(e)).equals(key(actualChildren.get(a)))) {
                compareChildren(expectedChildren.get(e), actualChildren.get(a), path + "/" + step(expectedChildren.get(e), expected));
                e++;
                a++;
            } else if (a == actualChildren.size()
                || (e < expectedChildren.size() && common[e + 1][a] >= common[e][a + 1])) {
                missing.add(expectedChildren.get(e++));
            } else {
                unexpected.add(actualChildren.get(a++));
            }
        }
        for (Node lost : missing) {
            String childPath = path + "/" + step(lost, expected);
            Node moved = unexpected.stream().filter(node -> key(node).equals(key(lost))).findFirst().orElse(null);
            if (moved == null) {
                report(childPath, "missing " + describe(lost));
            } else {
                unexpected.remove(moved);
                report(childPath, describe(lost) + " is out of order: expected as child " + (expectedChildren.indexOf(lost) + 1)
                    + " but found as child " + (actualChildren.indexOf(moved) + 1));
                compareChildren(lost, moved, childPath);
            }
        }
        for (Node extra : unexpected) {
            report(path + "/" + step(extra, actual), "unexpected " + describe(extra));
        }
    }

    private void compareChildren(Node expected, Node actual, String path) {
        if (expected instanceof Element) {
            compareElements((Element) expected, (Element) actual, path);
        } else if (!expected.getNodeValue().equals(actual.getNodeValue())) {
            report(path, "expected " + quote(expected.getNodeValue()) + " but was " + quote(actual.getNodeValue()));
        }
    }

    /** Children are paired in order by tag and id or name, so one inserted child does not shift the rest. */
    private static int[][] commonSubsequenceLengths(List<Node> expected, List<Node> actual) {
        int[][] lengths = new int[expected.size() + 1][actual.size() + 1];
        for (int e = expected.size() - 1; e >= 0; e--) {
            for (int a = actual.size() - 1; a >= 0; a--) {
                lengths[e][a] = key(expected.get(e)).equals(key(actual.get(a)))
                    ? lengths[e + 1][a + 1] + 1
                    : Math.max(lengths[e + 1][a], lengths[e][a + 1]);
            }
        }
        return lengths;
    }

    private static String key(Node node) {
        if (!(node instanceof Element)) {
            return "text()";
        }
        var element = (Element) node;
        return element.getTagName() + "|" + element.getAttribute("id") + "|" + element.getAttribute("name");
    }

    private void compareAttributes(Element expected, Element actual, String path) {
        var names = new TreeSet<String>();
        for (int index = 0; index < expected.getAttributes().getLength(); index++) {
            names.add(expected.getAttributes().item(index).getNodeName());
        }
        for (int index = 0; index < actual.getAttributes().getLength(); index++) {
            names.add(actual.getAttributes().item(index).getNodeName());
        }
        for (String name : names) {
            String attributePath = path + "/@" + name;
            if (!expected.hasAttribute(name)) {
                report(attributePath, "unexpected attribute with value " + quote(actual.getAttribute(name)));
            } else if (!actual.hasAttribute(name)) {
                report(attributePath, "missing attribute with value " + quote(expected.getAttribute(name)));
            } else if (!expected.getAttribute(name).equals(actual.getAttribute(name))) {
                report(attributePath, "expected " + quote(expected.getAttribute(name)) + " but was " + quote(actual.getAttribute(name)));
            }
        }
    }

    /** Elements and text, without whitespace-only text that only formats a list of elements. */
    private static List<Node> significantChildren(Element parent) {
        boolean hasElementChildren = false;
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            hasElementChildren |= child.getNodeType() == Node.ELEMENT_NODE;
        }
        var children = new ArrayList<Node>();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    children.add(child);
                    break;
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    if (!hasElementChildren || !child.getNodeValue().isBlank()) {
                        children.add(child);
                    }
                    break;
                default:
                    // Comments and processing instructions do not change a saved net.
            }
        }
        return children;
    }

    /** Names an element by id or name when it has one, otherwise by its position. */
    private static String step(Node node, Element parent) {
        if (!(node instanceof Element)) {
            return "text()";
        }
        var element = (Element) node;
        for (String key : List.of("id", "name")) {
            if (element.hasAttribute(key)) {
                return element.getTagName() + "[@" + key + "=" + quote(element.getAttribute(key)) + "]";
            }
        }
        int position = 1;
        for (Node sibling = parent.getFirstChild(); sibling != node; sibling = sibling.getNextSibling()) {
            if (sibling instanceof Element && ((Element) sibling).getTagName().equals(element.getTagName())) {
                position++;
            }
        }
        return element.getTagName() + "[" + position + "]";
    }

    private static String describe(Node node) {
        return node instanceof Element ? "element <" + ((Element) node).getTagName() + ">" : "text " + quote(node.getNodeValue());
    }

    private static String quote(String value) {
        return "'" + value.replace("\n", "\\n") + "'";
    }

    private void report(String path, String difference) {
        if (differences.size() < MAX_DIFFERENCES) {
            differences.add(path + ": " + difference);
        } else if (differences.size() == MAX_DIFFERENCES) {
            differences.add("... further differences omitted");
        }
    }
}
