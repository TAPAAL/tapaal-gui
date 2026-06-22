package dk.aau.cs.TCTL;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

//Represents a list of factors and the operators between then, these are all stored in the factors list
public class TCTLTermListNode extends TCTLAbstractStateProperty {

	final List<TCTLAbstractStateProperty> factors;

	public TCTLTermListNode(List<TCTLAbstractStateProperty> factors) {
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
    public void convertForReducedNet(String templateName) {
        for (TCTLAbstractProperty property : factors) {
            property.convertForReducedNet(templateName);
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
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(
			String templateName, String transitionName) {
		for(TCTLAbstractStateProperty factor : factors){
			if(factor.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName)){
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
    public boolean hasNestedPathQuantifiers() {
        return false;
    }

    @Override
    public StringPosition[] getChildren() {
        List<StringPosition> children = new ArrayList<>();
        int currentLength = 0;
        for (int i = 0; i < factors.size(); ++i) {
            var p = factors.get(i);
            int start = currentLength + (p.isSimpleProperty() ? 0 : 1);
            int end = start + p.toString().length();
            
            if (!(p instanceof AritmeticOperator)) {
                children.add(new StringPosition(start, end, p));
            }
            
            currentLength += (p.isSimpleProperty() ? p.toString().length() : p.toString().length() + 2);
            currentLength += 1;
        }

        return children.toArray(new StringPosition[0]);
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
	
	public String getOperator() {
		String op = null;
		for (TCTLAbstractStateProperty factor : factors) {
			if (factor instanceof AritmeticOperator) {
				if (op == null) {
					op = factor.toString();
				} else if (!op.equals(factor.toString())) {
					return null;
				}
			}
		}
        
		return op;
	}

	@Override
	public boolean isSimpleProperty() {
		if (factors.size() > 1) {
			if (parent instanceof TCTLTermListNode) {
				TCTLTermListNode p = (TCTLTermListNode) parent;
				String op = getOperator();
				String parentOp = p.getOperator();
				if (op != null && op.equals(parentOp)) {
					if (op.equals("+") || op.equals("*")) {
						return true;
					} else if (op.equals("-") && p.factors.indexOf(this) == 0){
						return true;
					}
				}
			}

			return false;
		}
        
		return true;
	}
}
