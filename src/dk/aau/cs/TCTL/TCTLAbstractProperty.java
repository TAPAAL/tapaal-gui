package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public abstract class TCTLAbstractProperty {

	// used to determine whether to put parenthesis around the property
	// when printing it to a string.
	public boolean isSimpleProperty() {
		return true;
	}

	// determine the object at a specified index in the string
	public StringPosition objectAt(int index) {
		StringPosition[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			StringPosition child = children[i];
			if (child.getStart() <= index && index <= child.getEnd()) {
				int start = child.getStart();
				return child.getObject().objectAt(index - start).addOffset(
						start);
			}
		}
		return new StringPosition(0, toString().length(), this);
	}

	// Determine the index of a specified property in the string
	public StringPosition indexOf(TCTLAbstractProperty property) {
		if (this == property) {
			return new StringPosition(0, toString().length(), this);
		} else {
			StringPosition[] children = getChildren();
			for (int i = 0; i < children.length; i++) {
				StringPosition position = children[i].getObject().indexOf(
						property);
				if (position != null) {
					return position.addOffset(children[i].getStart());
				}
			}
			return null;
		}
	}

	public abstract TCTLAbstractProperty copy();

	public abstract TCTLAbstractProperty replace(TCTLAbstractProperty object1,
			TCTLAbstractProperty object2);

	public StringPosition[] getChildren() {
		StringPosition[] children = {};
		return children;
	}

	public abstract void accept(ITCTLVisitor visitor, Object context);

	public abstract boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName);
	public abstract boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName);

	public abstract boolean containsPlaceHolder();

	// This method assumes that a place holder exists in the current query
	public abstract TCTLAbstractProperty findFirstPlaceHolder();

	

}
