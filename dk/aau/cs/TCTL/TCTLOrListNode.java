package dk.aau.cs.TCTL;

import java.util.ArrayList;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class TCTLOrListNode extends TCTLAbstractStateProperty {

	private ArrayList<TCTLAbstractStateProperty> properties;
	
	public void setProperties(ArrayList<TCTLAbstractStateProperty> properties) {
		this.properties = properties;
		
		for (TCTLAbstractStateProperty p : properties) {
			p.setParent(this);
		}
	}

	public ArrayList<TCTLAbstractStateProperty> getProperties() {
		return properties;
	}
	
	public TCTLOrListNode(ArrayList<TCTLAbstractStateProperty> properties) {
		this.properties = properties;
		
		for (TCTLAbstractStateProperty p : properties) {
			p.setParent(this);
		}		
	}
	
	public TCTLOrListNode(TCTLOrNode orNode) {
		this.properties = new ArrayList<TCTLAbstractStateProperty>();
		
		addDisjunct(orNode.getProperty1().copy());
		addDisjunct(orNode.getProperty2().copy());
	}
	
	public TCTLOrListNode() {
		this.properties = new ArrayList<TCTLAbstractStateProperty>();
		TCTLStatePlaceHolder ph = new TCTLStatePlaceHolder();
		ph.setParent(this);
		this.properties.add(ph);
		ph = new TCTLStatePlaceHolder();
		ph.setParent(this);
		this.properties.add(ph);
	}
	
	public void addDisjunct(TCTLAbstractStateProperty disjunct) {
		disjunct.setParent(this);
		properties.add(disjunct);
	}
	
	@Override
	public boolean isSimpleProperty() {
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		boolean firstTime = true;
		
		for (TCTLAbstractStateProperty prop : properties) {
			
			if(!firstTime){
				s.append(" or ");
			}
			
			s.append(prop.isSimpleProperty() ? prop.toString()
                    : "(" + prop.toString() + ")");
			firstTime = false;
		}
		
		return s.toString();
	}
	
	@Override
	public StringPosition[] getChildren() {
		StringPosition[] children = new StringPosition[properties.size()];
		
		int i = 0;
		int endPrev = 0;
		boolean wasPrevSimple = false;
		for (TCTLAbstractStateProperty p : properties) {
			
			int start = 0;
			int end = 0;
			
			if(i == 0) {
				wasPrevSimple = p.isSimpleProperty();
				start = wasPrevSimple ? 0 : 1;
				end = start + p.toString().length();
				
				endPrev = end;
				
			}
			else {
				start = endPrev + 4 + (p.isSimpleProperty() ? 0 : 1) + (wasPrevSimple ? 0 : 1);
				end = start + p.toString().length();
				
				endPrev = end;
				wasPrevSimple = p.isSimpleProperty();
			}
			
			StringPosition pos = new StringPosition(start, end, p);
			
			children[i] = pos;
			i++;
		}
		
		return children;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TCTLOrListNode) {
			TCTLOrListNode node = (TCTLOrListNode)o;
			return properties.equals(node.properties);
		}
		else if(o instanceof TCTLOrNode) {
			TCTLOrNode node = (TCTLOrNode)o;
			return properties.size() == 2 && properties.get(0).equals(node.getProperty1()) && properties.get(1).equals(node.getProperty2());
		}
		return false;
	}

	@Override
	public TCTLAbstractStateProperty copy() {
		ArrayList<TCTLAbstractStateProperty> copy = new ArrayList<TCTLAbstractStateProperty>();
		
		for (TCTLAbstractStateProperty p : properties) {
			copy.add(p.copy());
		}
		
		return new TCTLOrListNode(copy);
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1, TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty)object2;
			obj2.setParent(this.parent);
			return obj2;
		} 
		else {
			for (int i = 0; i < properties.size(); i++) {
				properties.set(i, properties.get(i).replace(object1, object2));
			}
			return this;
		}
	}

	@Override
	public void accept(ITCTLVisitor visitor) {
		visitor.visit(this);
		
	}

	@Override
	public boolean containsAtomicPropWithSpecificPlace(String placeName) {
		boolean atomicPropFound = false;
		
		for (TCTLAbstractStateProperty p : properties) {
			atomicPropFound = atomicPropFound || p.containsAtomicPropWithSpecificPlace(placeName);
		}
		
		return atomicPropFound;
	}

	@Override
	public boolean containsPlaceHolder() {
		boolean placeHolderFound = false;
		
		for (TCTLAbstractStateProperty p : properties) {
			placeHolderFound = placeHolderFound || p.containsPlaceHolder();
		}
		
		return placeHolderFound;
	}
	
	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		TCTLAbstractProperty ph = null;
		for (TCTLAbstractStateProperty p : properties) {
			if(p.containsPlaceHolder()) {
				ph = p.findFirstPlaceHolder();
				break;
			}
				
		}
		return ph;
	}

}

////returns the position in the string of the new selection.
//// When there is only one child property we want to select that child instead of the whole parent.
//// E.g. if selection is EF <*> then we want to select <*> to allow for speedier query construction.
//private StringPosition GetNewSelectionPosition(TCTLAbstractProperty newSelection) {
//
//	StringPosition position;
//
//
//	if(newSelection instanceof TCTLEFNode){
//		position = newProperty.indexOf(((TCTLEFNode)newSelection).getProperty());
//	}
//	else if(newSelection instanceof TCTLEGNode) {
//		position = newProperty.indexOf(((TCTLEGNode)newSelection).getProperty());
//	}
//	else if(newSelection instanceof TCTLAFNode) {
//		position = newProperty.indexOf(((TCTLAFNode)newSelection).getProperty());
//	}
//	else if(newSelection instanceof TCTLAGNode) {
//		position = newProperty.indexOf(((TCTLAGNode)newSelection).getProperty());
//	}
//	else if(newSelection instanceof TCTLNotNode) {
//		position = newProperty.indexOf(((TCTLNotNode)newSelection).getProperty());
//	}
//	else {
//		position = newProperty.indexOf(newSelection);
//	}
//
//	return position;
//}
