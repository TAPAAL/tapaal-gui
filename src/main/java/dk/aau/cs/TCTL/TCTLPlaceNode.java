package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;
import dk.aau.cs.io.NamePurifier;

public class TCTLPlaceNode extends TCTLAbstractStateProperty {
	private static final String SHARED = "Shared";

	String template;
	String place;
	private final String color;
	public TCTLPlaceNode(String template, String place) {
		this(template, place, null);
	}

	public TCTLPlaceNode(String template, String place, String color) {
		this.template = NamePurifier.purify(template);
		this.place = NamePurifier.purify(place);
		this.color = color == null ? null : color.trim();
	}

	public static TCTLPlaceNode fromManualQuery(String template, String place, String color) {
		return new TCTLPlaceNode(SHARED.equals(template) ? "" : template, place, color);
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
    public void convertForReducedNet(String templateName) {
	    if (template.isEmpty()) {
            place = "Shared__" + place;
        } else {
            place = template + "__" + place;
        }
        template = templateName;
    }

    @Override
	public TCTLAbstractStateProperty copy() {
		return new TCTLPlaceNode(template, place, color);
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
    public boolean hasNestedPathQuantifiers() {
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

	public String getColor() {
		return color;
	}

	@Override
	public String toString() {
		var prefix = template.isEmpty() ? color == null ? "" : SHARED + "." : template + ".";
		return prefix + place + (color == null ? "" : "." + color);
	}

    @Override
    public boolean equals(Object o) {
        if (o instanceof TCTLPlaceNode) {
            TCTLPlaceNode node = (TCTLPlaceNode)o;
            return template.equals(node.template) && place.equals(node.place);
        }

        return false;
    }
}
