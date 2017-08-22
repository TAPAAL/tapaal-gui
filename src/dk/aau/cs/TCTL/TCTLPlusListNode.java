package dk.aau.cs.TCTL;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

//Represents a list of terms and the operators between then, these are all stored in the terms list
public class TCTLPlusListNode extends TCTLAbstractStateProperty {

	ArrayList<TCTLAbstractStateProperty> terms;
	
	public TCTLPlusListNode(ArrayList<TCTLAbstractStateProperty> terms) {
		this.terms = terms;
		for(TCTLAbstractStateProperty term : terms){
			term.setParent(this);
		}
	}
	
	public List<TCTLAbstractStateProperty> getProperties(){
		return terms;
	}
	
	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(parent);
			return obj2;
		} else {
			for (int i = 0; i < terms.size(); i++) {
				terms.set(i, terms.get(i).replace(object1, object2));
			}
			return this;
		}
	}

	@Override
	public TCTLAbstractStateProperty copy() {
		ArrayList<TCTLAbstractStateProperty> copy = new ArrayList<TCTLAbstractStateProperty>();
		
		for(TCTLAbstractStateProperty term : terms){
			copy.add(term.copy());
		}
		
		return new TCTLPlusListNode(copy);
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);
	}

	@Override
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(
			String templateName, String placeName) {
		for(TCTLAbstractStateProperty term : terms){
			if(term.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(
			String templateName, String transitionName) {
		for(TCTLAbstractStateProperty term : terms){
			if(term.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsPlaceHolder() {
		for(TCTLAbstractStateProperty term : terms){
			if(term.containsPlaceHolder()){
				return true;
			}
		}
		return false;
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		for(TCTLAbstractStateProperty term : terms){
			TCTLAbstractProperty placeholder = term.findFirstPlaceHolder(); 
			if(placeholder != null){
				return placeholder;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(TCTLAbstractStateProperty term : terms){
			if(term.isSimpleProperty())
				sb.append(term);
			else
				sb.append("(" + term + ")");
				
			sb.append(" ");
		}
		
		return sb.toString().trim();
	}
}
