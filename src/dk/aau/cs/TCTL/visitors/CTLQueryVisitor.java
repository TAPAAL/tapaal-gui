package dk.aau.cs.TCTL.visitors;

import java.util.List;

import dk.aau.cs.TCTL.AritmeticOperator;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAUNode;
import dk.aau.cs.TCTL.TCTLAXNode;
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
import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.TCTL.TCTLPlusListNode;
import dk.aau.cs.TCTL.TCTLTermListNode;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.TCTL.visitors.QueryVisitor.QueryType;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;

public class CTLQueryVisitor extends VisitorBase {
	
	private static final String XML_HEADER 			= "<?xml version=\"1.0\"?>\n";
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
	private static final String XML_INTEGERLE 		= "integer-le";
	private static final String XML_INTEGERCONSTANT 	= "integer-constant";
	private static final String XML_TOKENSCOUNT 		= "tokens-count";
	private static final String XML_PLACE 			= "place";
	
	private StringBuffer XMLQuery;
	private TimedArcPetriNet model;
	
	public CTLQueryVisitor(TimedArcPetriNet model){
		this.model = model;
		this.XMLQuery = new StringBuffer(XML_HEADER);
	}
	
	public String getXMLQueryFor(TAPNQuery tapnQuery) {
		tapnQuery.getProperty().accept(this, null);
		return XMLQuery.toString();
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
		XMLQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL) + startTag(XML_ALLPATHS));
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
		XMLQuery.append(endTag(XML_REACH) + endTag(XML_UNTIL) + startTag(XML_EXISTSPATH));
	}

	public void visit(TCTLAndListNode andListNode, Object context) {
		createList(andListNode.getProperties(), context, XML_CONJUNCTION);
	}
	
	public void visit(TCTLOrListNode orListNode, Object context) {
		createList(orListNode.getProperties(), context, XML_DISJUNCTION);
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
		XMLQuery.append(wrapInTag(wrapInTag("1\n", XML_INTEGERCONSTANT) + wrapInTag("1\n", XML_INTEGERCONSTANT),XML_INTEGERLE));
	}
	
	public void visit(TCTLFalseNode tctlFalseNode, Object context) {
		XMLQuery.append(wrapInTag(wrapInTag("2\n", XML_INTEGERCONSTANT) + wrapInTag("1\n", XML_INTEGERCONSTANT),XML_INTEGERLE));
	}
	/*
	public void visit(TCTLDeadlockNode tctlDeadLockNode, Object context) {
		String transitionFireability;
		XMLQuery.append(startTag(XML_NEGATION) + startTag(XML_DISJUNCTION));
		
		for (TimedTransition transition : model.transitions()){
			transitionFireability = "";
			for (TimedInputArc arc : model.getInputArcsToTransition(transition)){
				
			}
			for (TransportArc arc : model.getTransportArcsToTransition(transition)){
				
			}
			
			if (transitionFireability.isEmpty()){ // Skip inhibitor arc check if there are no input arcs to the transition
				continue;
			}
			
			for (TimedInhibitorArc arc : model.getInhibitorArcsToTransition(transition)){
				
			}
		}
		
		XMLQuery.append(startTag(XML_DISJUNCTION) + startTag(XML_NEGATION));
	}*/
	
	public void visit(TCTLConstNode tctlConstNode, Object context){
		XMLQuery.append(wrapInTag(String.valueOf(tctlConstNode.getConstant()) + "\n", XML_INTEGERCONSTANT));
	}
	
	public void visit(TCTLPlaceNode tctlPlaceNode, Object context){
		XMLQuery.append(wrapInTag(wrapInTag(tctlPlaceNode.getPlace() + "\n", XML_PLACE), XML_TOKENSCOUNT));
	}
	
	@Override
	public void visit(TCTLAtomicPropositionNode atomicPropositionNode,
			Object context) {
		
		if (atomicPropositionNode.getOp().equals("<=")){
			XMLQuery.append(startTag(XML_INTEGERLE));
			atomicPropositionNode.getLeft().accept(this, context);
			atomicPropositionNode.getRight().accept(this, context);
			XMLQuery.append(endTag(XML_INTEGERLE));
		}
	}
	
	private String wrapInTag(String str, String tag){
		return startTag(tag) + str + endTag(tag);
	}
	private String startTag(String tag){
		return "<" + tag + ">\n";
	}
	private String endTag(String tag){
		return "<\\" + tag + ">\n";
	}
}
