package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.io.XMLFormatter;

import java.util.List;

public class CTLQueryVisitor extends VisitorBase {

    private static final String XML_NS = "xmlns=\"http://tapaal.net/\"";
    private static final String XML_PROPSET = "property-set";
    private static final String XML_PROP = "property";
    private static final String XML_PROPID = "id";
    private static final String XML_PROPDESC = "description";
    private static final String XML_FORMULA = "formula";
    private static final String XML_ALLPATHS = "all-paths";
    private static final String XML_EXISTSPATH = "exists-path";
    private static final String XML_NEGATION = "negation";
    private static final String XML_CONJUNCTION = "conjunction";
    private static final String XML_DISJUNCTION = "disjunction";
    private static final String XML_GLOBALLY = "globally";
    private static final String XML_FINALLY = "finally";
    private static final String XML_NEXT = "next";
    private static final String XML_UNTIL = "until";
    private static final String XML_BEFORE = "before";
    private static final String XML_REACH = "reach";
    private static final String XML_DEADLOCK = "deadlock";
    private static final String XML_TRUE = "true";
    private static final String XML_FALSE = "false";
    private static final String XML_INTEGERLT = "integer-lt";
    private static final String XML_INTEGERLE = "integer-le";
    private static final String XML_INTEGEREQ = "integer-eq";
    private static final String XML_INTEGERNE = "integer-ne";
    private static final String XML_INTEGERGT = "integer-gt";
    private static final String XML_INTEGERGE = "integer-ge";
    private static final String XML_ISFIREABLE = "is-fireable";
    private static final String XML_INTEGERCONSTANT = "integer-constant";
    private static final String XML_TRANSITION = "transition";
    private static final String XML_INTEGERSUM = "integer-sum";
    private static final String XML_INTEGERPRODUCT = "integer-product";
    private static final String XML_INTEGERDIFFERENCE = "integer-difference";
    private static final String CONTROL = "control";

    private final StringBuffer xmlQuery = new StringBuffer();

    public CTLQueryVisitor() {
        this(false);
    }

    public CTLQueryVisitor(boolean legacy) {
        super(legacy);
    }

    public String getXMLQueryFor(TCTLAbstractProperty property, String queryName, boolean control) {
        buildXMLQuery(property, queryName, control);
        return getFormatted();
    }

    public void buildXMLQuery(TCTLAbstractProperty property, String queryName, boolean control) {
        xmlQuery.append(startTag(XML_PROP) + queryInfo(queryName) + startTag(XML_FORMULA));
        if (control) xmlQuery.append(startTag(CONTROL));
        property.accept(this, null);
        if (control) xmlQuery.append(endTag(CONTROL));
        xmlQuery.append(endTag(XML_FORMULA) + endTag(XML_PROP));
    }

    public String getFormatted() {
        XMLFormatter formatter = new XMLFormatter();
        return formatter.format(getStartTag() + xmlQuery.toString() + getEndTag());
    }

    public StringBuffer getXMLQuery() {
        return xmlQuery;
    }

    public String getStartTag() {
        return startTag(XML_PROPSET + " " + XML_NS) + "\n";
    }

    public String getEndTag() {
        return endTag(XML_PROPSET) + "\n";
    }

    private String queryInfo(String queryName) {
        String nameToPrint = (queryName == null) ? "Query Comment/Name Here" : queryName;
        return wrapInTag(nameToPrint, XML_PROPID) + wrapInTag(nameToPrint, XML_PROPDESC);
    }

    public void visit(TCTLAFNode afNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_FINALLY));
        afNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_FINALLY) + endTag(XML_ALLPATHS));
    }

    public void visit(TCTLAGNode agNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_GLOBALLY));
        agNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_GLOBALLY) + endTag(XML_ALLPATHS));
    }

    public void visit(TCTLAXNode axNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_NEXT));
        axNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_NEXT) + endTag(XML_ALLPATHS));
    }

    public void visit(TCTLAUNode auNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_UNTIL) + startTag(XML_BEFORE));
        auNode.getLeft().accept(this, context);
        xmlQuery.append(endTag(XML_BEFORE) + startTag(XML_REACH));
        auNode.getRight().accept(this, context);
        xmlQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL) + endTag(XML_ALLPATHS));
    }

    public void visit(TCTLEFNode efNode, Object context) {
        xmlQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_FINALLY));
        efNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_FINALLY) + endTag(XML_EXISTSPATH));
    }

    public void visit(TCTLEGNode egNode, Object context) {
        xmlQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_GLOBALLY));
        egNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_GLOBALLY) + endTag(XML_EXISTSPATH));
    }

    public void visit(TCTLEXNode exNode, Object context) {
        xmlQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_NEXT));
        exNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_NEXT) + endTag(XML_EXISTSPATH));
    }

    public void visit(TCTLEUNode euNode, Object context) {
        xmlQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_UNTIL) + startTag(XML_BEFORE));
        euNode.getLeft().accept(this, context);
        xmlQuery.append(endTag(XML_BEFORE) + startTag(XML_REACH));
        euNode.getRight().accept(this, context);
        xmlQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL) + endTag(XML_EXISTSPATH));
    }

    public void visit(LTLFNode afNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_FINALLY));
        afNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_FINALLY) + endTag(XML_ALLPATHS));
    }

    public void visit(LTLGNode agNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_GLOBALLY));
        agNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_GLOBALLY) + endTag(XML_ALLPATHS));
    }

    public void visit(LTLXNode axNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_NEXT));
        axNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_NEXT) + endTag(XML_ALLPATHS));
    }

    public void visit(LTLUNode auNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS) + startTag(XML_UNTIL) + startTag(XML_BEFORE));
        auNode.getLeft().accept(this, context);
        xmlQuery.append(endTag(XML_BEFORE) + startTag(XML_REACH));
        auNode.getRight().accept(this, context);
        xmlQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL) + endTag(XML_ALLPATHS));
    }

    public void visit(TCTLPathToStateConverter pathConverter, Object context) {
        pathConverter.getProperty().accept(this, context);
    }

    public void visit(TCTLAndListNode andListNode, Object context) {
        createList(andListNode.getProperties(), context, XML_CONJUNCTION);
    }

    public void visit(TCTLOrListNode orListNode, Object context) {
        createList(orListNode.getProperties(), context, XML_DISJUNCTION);
    }

    public void visit(TCTLTermListNode termListNode, Object context) {
        if (termListNode.getProperties().size() > 1) {
            assert termListNode.getProperties().get(1) instanceof AritmeticOperator;
            AritmeticOperator operator = (AritmeticOperator) termListNode.getProperties().get(1);
            String op = operator.toString();
            switch (op) {
                case "+":
                    createList(termListNode.getProperties(), context, XML_INTEGERSUM);
                    break;
                case "*":
                    createList(termListNode.getProperties(), context, XML_INTEGERPRODUCT);
                    break;
                case "-":
                    createList(termListNode.getProperties(), context, XML_INTEGERDIFFERENCE);
                    break;
            }
        } else {
            termListNode.getProperties().get(0).accept(this, context);
        }

    }

    private void createList(List<TCTLAbstractStateProperty> properties, Object context, String seperator) {
        xmlQuery.append(startTag(seperator));

        for (TCTLAbstractStateProperty p : properties) {
            p.accept(this, context);
        }

        xmlQuery.append(endTag(seperator));
    }

    public void visit(TCTLNotNode notNode, Object context) {
        xmlQuery.append(startTag(XML_NEGATION));
        notNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_NEGATION));
    }

    public void visit(TCTLTrueNode tctlTrueNode, Object context) {
        xmlQuery.append(emptyElement(XML_TRUE));
    }

    public void visit(TCTLFalseNode tctlFalseNode, Object context) {
        xmlQuery.append(emptyElement(XML_FALSE));
    }

    public void visit(TCTLDeadlockNode tctlDeadLockNode, Object context) {
        xmlQuery.append(emptyElement(XML_DEADLOCK));
    }

    public void visit(TCTLConstNode tctlConstNode, Object context) {
        xmlQuery.append(wrapInTag(tctlConstNode.getConstant() + "", XML_INTEGERCONSTANT));
    }

    public void visit(TCTLPlaceNode tctlPlaceNode, Object context) {
        xmlQuery.append(startTag(XML_TOKENSCOUNT));
        xmlQuery.append(createXMLPlace(tctlPlaceNode, legacy));
        xmlQuery.append(endTag(XML_TOKENSCOUNT));
    }

    public void visit(TCTLTransitionNode tctlTransitionNode, Object context) {
        xmlQuery.append(startTag(XML_ISFIREABLE));
        xmlQuery.append(wrapInTag(tctlTransitionNode.toString() + "", XML_TRANSITION));
        xmlQuery.append(endTag(XML_ISFIREABLE));
    }

    @Override
    public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) {
        String opTest = atomicPropositionNode.getOp();
        String op;

        switch (opTest) {
            case "<":
                op = XML_INTEGERLT;
                break;
            case "<=":
                op = XML_INTEGERLE;
                break;
            case "=":
                op = XML_INTEGEREQ;
                break;
            case "!=":
                op = XML_INTEGERNE;
                break;
            case ">":
                op = XML_INTEGERGT;
                break;
            case ">=":
                op = XML_INTEGERGE;
                break;
            default:
                op = "MISSING_OPERATOR";
                break;
        }

        xmlQuery.append(startTag(op));
        atomicPropositionNode.getLeft().accept(this, context);
        atomicPropositionNode.getRight().accept(this, context);
        xmlQuery.append(endTag(op));
    }

    private String wrapInTag(String str, String tag) {
        return startTag(tag) + str + endTag(tag);
    }

    private String startTag(String tag) {
        return "<" + tag + ">";
    }

    private String endTag(String tag) {
        return "</" + tag + ">";
    }

    private String emptyElement(String tag) {
        return startTag(tag + "/");
    }
}
