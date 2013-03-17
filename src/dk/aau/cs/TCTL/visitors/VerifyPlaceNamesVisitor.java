package dk.aau.cs.TCTL.visitors;

import java.util.ArrayList;
import java.util.HashSet;

import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLAbstractStateProperty;
import dk.aau.cs.TCTL.TCTLAndListNode;
import dk.aau.cs.TCTL.TCTLAtomicPropositionNode;
import dk.aau.cs.TCTL.TCTLDeadLockNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.TCTL.TCTLFalseNode;
import dk.aau.cs.TCTL.TCTLNotNode;
import dk.aau.cs.TCTL.TCTLOrListNode;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import dk.aau.cs.TCTL.TCTLStatePlaceHolder;
import dk.aau.cs.TCTL.TCTLTrueNode;
import dk.aau.cs.util.Tuple;

public class VerifyPlaceNamesVisitor implements ITCTLVisitor {

	private ArrayList<Tuple<String, String>> templatePlaceNames;

	public VerifyPlaceNamesVisitor(ArrayList<Tuple<String, String>> templatePlaceNames) {
		this.templatePlaceNames = templatePlaceNames;
	}

	public Context verifyPlaceNames(TCTLAbstractProperty query) {
		Context c = new Context();

		query.accept(this, c);

		return c;
	}

	public void visit(TCTLAFNode afNode, Object context) {
		afNode.getProperty().accept(this, context);
	}

	public void visit(TCTLAGNode agNode, Object context) {
		agNode.getProperty().accept(this, context);
	}

	public void visit(TCTLEFNode efNode, Object context) {
		efNode.getProperty().accept(this, context);
	}

	public void visit(TCTLEGNode egNode, Object context) {
		egNode.getProperty().accept(this, context);
	}

	public void visit(TCTLAtomicPropositionNode atomicPropositionNode, Object context) {
		Context c = (Context) context;
		if (!templatePlaceNames.contains(new Tuple<String,String>(atomicPropositionNode.getTemplate(), atomicPropositionNode.getPlace()))) {
			c.addIncorrectPlaceName(atomicPropositionNode.getTemplate() + "." + atomicPropositionNode.getPlace());
			c.setResult(false);
		}
	}

	public void visit(TCTLStatePlaceHolder statePlaceHolderNode, Object context) {
	}

	public void visit(TCTLPathPlaceHolder pathPlaceHolderNode, Object context) {
	}

	public void visit(TCTLNotNode notNode, Object context) {
		notNode.getProperty().accept(this, context);
	}

	public void visit(TCTLAndListNode andListNode, Object context) {
		for (TCTLAbstractStateProperty p : andListNode.getProperties()) {
			p.accept(this, context);
		}
	}

	public void visit(TCTLOrListNode orListNode, Object context) {
		for (TCTLAbstractStateProperty p : orListNode.getProperties()) {
			p.accept(this, context);
		}
	}

	// / context class
	public class Context {
		private Boolean result;
		private HashSet<String> incorrectPlaceNames;

		public Boolean getResult() {
			return result;
		}

		public void setResult(Boolean result) {
			this.result = result;
		}

		public HashSet<String> getIncorrectPlaceNames() {
			return incorrectPlaceNames;
		}

		public void addIncorrectPlaceName(String incorrectPlaceName) {
			incorrectPlaceNames.add(incorrectPlaceName);
		}

		public Context() {
			result = true;
			incorrectPlaceNames = new HashSet<String>();
		}
	}

	public void visit(TCTLTrueNode tctlTrueNode, Object context) {
	}

	public void visit(TCTLFalseNode tctlFalseNode, Object context) {
	}
	
	public void visit(TCTLDeadLockNode tctlDeadLockNode, Object context) {
	}

}
