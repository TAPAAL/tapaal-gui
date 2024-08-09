package dk.aau.cs.TCTL.visitors;

import java.util.List;

import dk.aau.cs.TCTL.*;
import dk.aau.cs.io.XMLFormatter;

public class LTLQueryVisitor extends VisitorBase {

    protected static final String XML_NS 			        = "xmlns=\"http://tapaal.net/\"";
    protected static final String XML_PROPSET 		    = "property-set";
    protected static final String XML_PROP			    = "property";
    protected static final String XML_PROPID			    = "id";
    protected static final String XML_PROPDESC		    = "description";
    protected static final String XML_FORMULA		    	= "formula";
    protected static final String XML_ALLPATHS	    	= "all-paths";
    protected static final String XML_EXISTSPATH	    	= "exists-path";
    protected static final String XML_NEGATION	    	= "negation";
    protected static final String XML_CONJUNCTION 		= "conjunction";
    protected static final String XML_DISJUNCTION 		= "disjunction";
    protected static final String XML_GLOBALLY		    = "globally";
    protected static final String XML_FINALLY			    = "finally";
    protected static final String XML_NEXT			    = "next";
    protected static final String XML_UNTIL 		    	= "until";
    protected static final String XML_BEFORE 			    = "before";
    protected static final String XML_REACH 		    	= "reach";
    protected static final String XML_DEADLOCK 		    = "deadlock";
    protected static final String XML_TRUE 			    = "true";
    protected static final String XML_FALSE 			    = "false";
    protected static final String XML_INTEGERLT 		    = "integer-lt";
    protected static final String XML_INTEGERLE 		    = "integer-le";
    protected static final String XML_INTEGEREQ		    = "integer-eq";
    protected static final String XML_INTEGERNE 		    = "integer-ne";
    protected static final String XML_INTEGERGT 		    = "integer-gt";
    protected static final String XML_INTEGERGE           = "integer-ge";
    protected static final String XML_ISFIREABLE		    = "is-fireable";
    protected static final String XML_INTEGERCONSTANT 	= "integer-constant";
    protected static final String XML_TOKENSCOUNT 		= "tokens-count";
    protected static final String XML_PLACE 			    = "place";
    protected static final String XML_TRANSITION		    = "transition";
    protected static final String XML_INTEGERSUM          = "integer-sum";
    protected static final String XML_INTEGERPRODUCT      = "integer-product";
    protected static final String XML_INTEGERDIFFERENCE   = "integer-difference";


    protected final StringBuffer xmlQuery = new StringBuffer();

    public LTLQueryVisitor() {
        super();
    }

    public String getXMLQueryFor(TCTLAbstractProperty property, String queryName) {
        buildXMLQuery(property, queryName);
        return getFormatted();
    }

    public StringBuffer getXMLQuery() {
        return xmlQuery;
    }

    public void buildXMLQuery(TCTLAbstractProperty property, String queryName) {
        xmlQuery.append(startTag(XML_PROP) + queryInfo(queryName) + startTag(XML_FORMULA));
        property.accept(this, null);
        xmlQuery.append(endTag(XML_FORMULA) + endTag(XML_PROP));
    }

    public String getFormatted() {
        XMLFormatter formatter = new XMLFormatter();
        return formatter.format(getStartTag() + xmlQuery.toString() + getEndTag());
    }

    public String getFormatted(StringBuffer CTLQueries) {
        XMLFormatter formatter = new XMLFormatter();
        xmlQuery.append(CTLQueries);
        return formatter.format(getStartTag() + xmlQuery.toString() + getEndTag());
    }

    public String getStartTag(){
        return startTag(XML_PROPSET + " " + XML_NS) + "\n";
    }

    public String getEndTag(){
        return endTag(XML_PROPSET) + "\n";
    }

    protected String queryInfo(String queryName){
        String nameToPrint = (queryName == null) ? "Query Comment/Name Here" : queryName;
        return wrapInTag(nameToPrint, XML_PROPID) + wrapInTag(nameToPrint, XML_PROPDESC);
    }

    public void visit(LTLANode aNode, Object context) {
        xmlQuery.append(startTag(XML_ALLPATHS));
        aNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_ALLPATHS));
    }

    public void visit(LTLENode eNode, Object context) {
        xmlQuery.append(startTag(XML_EXISTSPATH));
        eNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_EXISTSPATH));
    }

    public void visit(LTLFNode afNode, Object context) {
        xmlQuery.append(startTag(XML_FINALLY));
        afNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_FINALLY));
    }

    public void visit(LTLGNode agNode, Object context) {
        xmlQuery.append(startTag(XML_GLOBALLY));
        agNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_GLOBALLY));
    }

    public void visit(LTLXNode axNode, Object context) {
        xmlQuery.append(startTag(XML_NEXT));
        axNode.getProperty().accept(this, context);
        xmlQuery.append(endTag(XML_NEXT));
    }

    public void visit(LTLUNode auNode, Object context) {
        xmlQuery.append(startTag(XML_UNTIL) + startTag(XML_BEFORE));
        auNode.getLeft().accept(this, context);
        xmlQuery.append(endTag(XML_BEFORE) + startTag(XML_REACH));
        auNode.getRight().accept(this, context);
        xmlQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL));
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
        assert termListNode.getProperties().get(1) instanceof AritmeticOperator;
        AritmeticOperator operator = (AritmeticOperator)termListNode.getProperties().get(1);
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

    public void visit(TCTLConstNode tctlConstNode, Object context){
        xmlQuery.append(wrapInTag(tctlConstNode.getConstant() + "", XML_INTEGERCONSTANT));
    }

    public void visit(TCTLPlaceNode tctlPlaceNode, Object context){
        xmlQuery.append(startTag(XML_TOKENSCOUNT));
        xmlQuery.append(wrapInTag(tctlPlaceNode.toString() + "", XML_PLACE));
        xmlQuery.append(endTag(XML_TOKENSCOUNT));
    }

    public void visit(TCTLTransitionNode tctlTransitionNode, Object context){
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

    protected String wrapInTag(String str, String tag){
        return startTag(tag) + str + endTag(tag);
    }
    protected String startTag(String tag){
        return "<" + tag + ">";
    }
    protected String endTag(String tag){
        return "</" + tag + ">";
    }
    protected String emptyElement(String tag){
        return startTag(tag + "/");
    }
}
