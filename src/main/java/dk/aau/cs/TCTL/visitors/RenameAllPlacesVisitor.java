package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.verification.NameMapping;

public class RenameAllPlacesVisitor extends VisitorBase {

	private final NameMapping mapping;

	public RenameAllPlacesVisitor(NameMapping mapping) {
		this.mapping = mapping;
	}

	public void visit(TCTLPlaceNode placeNode,
			Object context) {
		String mappedName = mapping.map(placeNode.getTemplate(),
				placeNode.getPlace());
		if (mappedName != null) {
			placeNode.setPlace(mappedName);
			placeNode.setTemplate("");
		}
	}
}
