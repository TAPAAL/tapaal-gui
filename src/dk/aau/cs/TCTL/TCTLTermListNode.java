package dk.aau.cs.TCTL;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

//Represents a list of factors and the operators between then, these are all stored in the factors list
public class TCTLTermListNode extends TCTLAbstractStateProperty {

	ArrayList<TCTLAbstractStateProperty> factors;

	public TCTLTermListNode(ArrayList<TCTLAbstractStateProperty> factors) {
		this.factors = factors;
		for(TCTLAbstractStateProperty factor: factors){
			factor.setParent(this);
		}
	}

	public List<TCTLAbstractStateProperty> getProperties(){
		return factors;
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(parent);
			return obj2;
		} else {
			for (int i = 0; i < factors.size(); i++) {
				factors.set(i, factors.get(i).replace(object1, object2));
			}
			return this;
		}
	}

	@Override
	public TCTLAbstractStateProperty copy() {
		ArrayList<TCTLAbstractStateProperty> copy = new ArrayList<TCTLAbstractStateProperty>();

		for(TCTLAbstractStateProperty factor : factors){
			copy.add(factor.copy());
		}

		return new TCTLTermListNode(copy);
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);
	}

	@Override
	public boolean containsAtomicPropWithSpecificPlace(String placeName) {
		for(TCTLAbstractStateProperty factor : factors){
			if(factor.containsAtomicPropWithSpecificPlace(placeName)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(
			String templateName, String placeName) {
		for(TCTLAbstractStateProperty factor : factors){
			if(factor.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsPlaceHolder() {
		for(TCTLAbstractStateProperty factor : factors){
			if(factor.containsPlaceHolder()){
				return true;
			}
		}
		return false;
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		for(TCTLAbstractStateProperty factor : factors){
			TCTLAbstractProperty placeholder = factor.findFirstPlaceHolder(); 
			if(placeholder != null){
				return placeholder;
			}
		}
		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(TCTLAbstractStateProperty factor : factors){
			if(factor.isSimpleProperty())
				sb.append(factor);
			else 
				sb.append("(" + factor + ")");
			sb.append(" ");
		}

		return sb.toString().trim();
	}
}
