package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;

public class RenamePlaceTCTLVisitor extends VisitorBase{

	private String oldPlaceName;
	private String newPlaceName;

	public RenamePlaceTCTLVisitor(String oldName, String newName) {
		oldPlaceName = oldName;
		newPlaceName = newName;
	}

	public void visit(TCTLPlaceNode placeNode,
			Object context) {
		if (placeNode.getPlace().equals(oldPlaceName)) {
			placeNode.setPlace(newPlaceName);
		}
	}
}
