package dk.aau.cs.TCTL.visitors;

import java.util.Iterator;
import java.util.List;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAUNode;
import dk.aau.cs.TCTL.TCTLAXNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLConstNode;
import dk.aau.cs.TCTL.TCTLDeadlockNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLEUNode;
import dk.aau.cs.TCTL.TCTLEXNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathToStateConverter;
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.TCTL.TCTLPlusListNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.io.XMLFormatter;
import pipe.dataLayer.TAPNQuery;

public class CTLQueryVisitor extends VisitorBase {
	
	private static final String XML_HEADER 			= "<?xml version=\"1.0\"?>";
	private static final String XML_NS 				= "xmlns=\"http://tapaal.net/\"";
	private static final String XML_PROPSET 			= "property-set";
	private static final String XML_PROP				= "property";
	private static final String XML_PROPID			= "id";
	private static final String XML_PROPDESC			= "description";
	private static final String XML_FORMULA			= "formula";	
	private static final String XML_ALLPATHS			= "all-paths";
	private static final String XML_EXISTSPATH	 	= "exists-path";
	private static final String XML_NEGATION			= "negation";
	private static final String XML_CONJUNCTION 		= "conjunction";
	private static final String XML_DISJUNCTION 		= "disjunction";
	private static final String XML_GLOBALLY			= "globally";
	private static final String XML_FINALLY			= "finally";
	private static final String XML_NEXT				= "next";
	private static final String XML_UNTIL 			= "until";
	private static final String XML_BEFORE 			= "before";
	private static final String XML_REACH 			= "reach";
	private static final String XML_DEADLOCK 			= "deadlock";
	private static final String XML_TRUE 			= "true";
	private static final String XML_FALSE 			= "false";
	private static final String XML_INTEGERLT 		= "integer-lt";
	private static final String XML_INTEGERLE 		= "integer-le";
	private static final String XML_INTEGEREQ			= "integer-eq";
	private static final String XML_INTEGERNE 		= "integer-ne";
	private static final String XML_INTEGERGT 		= "integer-gt";
	private static final String XML_INTEGERGE			= "integer-ge";
	private static final String XML_ISFIREABLE		= "is-fireable";
	private static final String XML_INTEGERCONSTANT 	= "integer-constant";
	private static final String XML_TOKENSCOUNT 		= "tokens-count";
	private static final String XML_PLACE 			= "place";
	private static final String XML_TRANSITION		= "transition";
	
	private StringBuffer XMLQuery;
	
	public String getXMLQueryFor(TCTLAbstractProperty property, String queryName) {
		this.XMLQuery = new StringBuffer();
		XMLQuery.append(XML_HEADER + startTag(XML_PROPSET + " " + XML_NS ) + startTag(XML_PROP) + queryInfo(queryName) + startTag(XML_FORMULA));
		property.accept(this, null);
		XMLQuery.append(endTag(XML_FORMULA) + endTag(XML_PROP) + endTag(XML_PROPSET));
		XMLFormatter formatter = new XMLFormatter();
		return formatter.format(XMLQuery.toString());
	}
	
	public String getXMLQueriesFor(Iterator<pipe.dataLayer.TAPNQuery> iterator){
		this.XMLQuery = new StringBuffer();
		
		XMLQuery.append(XML_HEADER + startTag(XML_PROPSET + " " + XML_NS));
		while (iterator.hasNext()){
		    TAPNQuery query = iterator.next();
			XMLQuery.append(startTagInline(XML_PROP) + queryInfo(query.getName()) + startTag(XML_FORMULA));
			query.getProperty().accept(this, null);
			XMLQuery.append(endTag(XML_FORMULA) + endTag(XML_PROP));
		}
		XMLQuery.append(endTag(XML_PROPSET));
		XMLFormatter formatter = new XMLFormatter();
		return formatter.format(XMLQuery.toString());
	}
	
	private String queryInfo(String queryName){
		String nameToPrint = (queryName == null) ? "Query Comment/Name Here" : queryName;
		return wrapInTag(nameToPrint, XML_PROPID) + wrapInTag(nameToPrint, XML_PROPDESC);
	}
	
	public void visit(TCTLAFNode afNode, Object context) {
		XMLQuery.append(startTag(XML_ALLPATHS) + startTag(XML_FINALLY));
		afNode.getProperty().accept(this, context);
		XMLQuery.append(endTag(XML_FINALLY) + endTag(XML_ALLPATHS));
	}

	public void visit(TCTLAGNode agNode, Object context) {
		XMLQuery.append(startTag(XML_ALLPATHS) + startTag(XML_GLOBALLY));
		agNode.getProperty().accept(this, context);
		XMLQuery.append(endTag(XML_GLOBALLY) + endTag(XML_ALLPATHS));
	}
	
	public void visit(TCTLAXNode axNode, Object context) {
		XMLQuery.append(startTag(XML_ALLPATHS) + startTag(XML_NEXT));
		axNode.getProperty().accept(this, context);
		XMLQuery.append(endTag(XML_NEXT) + endTag(XML_ALLPATHS));
	}
	
	public void visit(TCTLAUNode auNode, Object context) {
		XMLQuery.append(startTag(XML_ALLPATHS) + startTag(XML_UNTIL) + startTag(XML_BEFORE));
		auNode.getLeft().accept(this, context);
		XMLQuery.append(endTag(XML_BEFORE) + startTag(XML_REACH));
		auNode.getRight().accept(this, context);
		XMLQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL) + endTag(XML_ALLPATHS));
	}

	public void visit(TCTLEFNode efNode, Object context) {
		XMLQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_FINALLY));
		efNode.getProperty().accept(this, context);
		XMLQuery.append(endTag(XML_FINALLY) + endTag(XML_EXISTSPATH));
	}

	public void visit(TCTLEGNode egNode, Object context) {
		XMLQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_GLOBALLY));
		egNode.getProperty().accept(this, context);
		XMLQuery.append(endTag(XML_GLOBALLY) + endTag(XML_EXISTSPATH));
	}
	
	public void visit(TCTLEXNode exNode, Object context) {
		XMLQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_NEXT));
		exNode.getProperty().accept(this, context);
		XMLQuery.append(endTag(XML_NEXT) + endTag(XML_EXISTSPATH));
	}
	
	public void visit(TCTLEUNode euNode, Object context) {
		XMLQuery.append(startTag(XML_EXISTSPATH) + startTag(XML_UNTIL) + startTag(XML_BEFORE));
		euNode.getLeft().accept(this, context);
		XMLQuery.append(endTag(XML_BEFORE) + startTag(XML_REACH));
		euNode.getRight().accept(this, context);
		XMLQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL) + endTag(XML_EXISTSPATH));
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
	
	public void visit(TCTLPlusListNode plusListNode, Object context) {
		if (plusListNode.getProperties().get(0) instanceof TCTLTransitionNode){
			createList(plusListNode.getProperties(), context, XML_ISFIREABLE);
		} else if (plusListNode.getProperties().get(0) instanceof TCTLPlaceNode){
			createList(plusListNode.getProperties(), context, XML_TOKENSCOUNT);
		}
	}

	private void createList(List<TCTLAbstractStateProperty> properties, Object context, String seperator) {
		XMLQuery.append(startTag(seperator));

		for (TCTLAbstractStateProperty p : properties) {
			p.accept(this, context);
		}

		XMLQuery.append(endTag(seperator));
	}

	public void visit(TCTLNotNode notNode, Object context) {
		XMLQuery.append(startTag(XML_NEGATION));
		notNode.getProperty().accept(this, context);
		XMLQuery.append(endTag(XML_NEGATION));
	}
	
	public void visit(TCTLTrueNode tctlTrueNode, Object context) {		
		XMLQuery.append(emptyElement(XML_TRUE));
	}
	
	public void visit(TCTLFalseNode tctlFalseNode, Object context) {
		XMLQuery.append(emptyElement(XML_FALSE));
	}
	
	public void visit(TCTLDeadlockNode tctlDeadLockNode, Object context) {
		XMLQuery.append(emptyElement(XML_DEADLOCK));
	}
	
	public void visit(TCTLConstNode tctlConstNode, Object context){
		XMLQuery.append(wrapInTag(String.valueOf(tctlConstNode.getConstant()) + "", XML_INTEGERCONSTANT));
	}
	
	public void visit(TCTLPlaceNode tctlPlaceNode, Object context){
		String placeName = (tctlPlaceNode.getTemplate().isEmpty() ?
				tctlPlaceNode.getPlace() :
				tctlPlaceNode.getTemplate() + "." + tctlPlaceNode.getPlace());
		XMLQuery.append(wrapInTag(placeName + "", XML_PLACE));
	}
	
	public void visit(TCTLTransitionNode tctlTransitionNode, Object context){
		XMLQuery.append(wrapInTag(tctlTransitionNode.getTransition() + "", XML_TRANSITION));
	}
	
	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context) {
		String opTest = atomicPropositionNode.getOp();
		String op = new String();
		
		if (opTest.equals("<")){
			op = XML_INTEGERLT;
		} else if(opTest.equals("<=")){
			op = XML_INTEGERLE;
		} else if(opTest.equals("=")){
			op = XML_INTEGEREQ;
		} else if(opTest.equals("!=")){
			op = XML_INTEGERNE;
		} else if(opTest.equals(">")){
			op = XML_INTEGERGT;
		} else if(opTest.equals(">=")){
			op = XML_INTEGERGE;
		} else {
			op = "MISSING_OPERATOR";
		}
		
		XMLQuery.append(startTag(op));
		atomicPropositionNode.getLeft().accept(this, context);
		atomicPropositionNode.getRight().accept(this, context);
		XMLQuery.append(endTag(op));
	}
	
	private String wrapInTag(String str, String tag){
		return startTag(tag) + str + endTag(tag);
	}
	private String startTagInline(String tag){
		return "<" + tag + ">";
	}
	private String startTag(String tag){
		return "<" + tag + ">";
	}
	private String endTag(String tag){
		return "</" + tag + ">";
	}
	private String emptyElement(String tag){
		return startTag(tag + "/");
	}	
}
