package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;
import dk.aau.cs.io.NamePurifier;

public class TCTLPlaceNode extends TCTLAbstractStateProperty {

	String template;
	String place;
	
	public TCTLPlaceNode(String template, String place) {
		this.template = NamePurifier.purify(template);
		this.place = NamePurifier.purify(place);
	}

	public TCTLPlaceNode(String place) {
		this("", place);
	}

	@Override
	public TCTLAbstractStateProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2) {
		if (this == object1 && object2 instanceof TCTLAbstractStateProperty) {
			TCTLAbstractStateProperty obj2 = (TCTLAbstractStateProperty) object2;
			obj2.setParent(parent);
			return obj2;
		} else {
			return this;
		}
	}

	@Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLPlaceNode(template, place);
	}

	@Override
	public void accept(ITCTLVisitor visitor, Object context) {
		visitor.visit(this, context);

	}

	@Override
	public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(
			String templateName, String placeName) {
		return place.equals(placeName) && template.equals(templateName);
	}
	
	@Override
	public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(
			String templateName, String transitionName) {
		return false;
	}

	@Override
	public boolean containsPlaceHolder() {
		return false;
	}

	@Override
	public TCTLAbstractProperty findFirstPlaceHolder() {
		return null;
	}

	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getPlace() {
		return place;
	}
	
	public void setPlace(String place) {
		this.place = place;
	}
	
	@Override
	public String toString() {
		return (template == "" ? "" : template + ".") + place;
	}
}
