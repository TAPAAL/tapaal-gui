package dk.aau.cs.TCTL.visitors;

import dk.aau.cs.TCTL.TCTLPlaceNode;
import dk.aau.cs.util.Require;

public class MakePlaceSharedVisitor extends VisitorBase {
	private final String oldTemplateName;
	private final String oldPlaceName;
	private final String newTemplateName;
	private final String newPlaceName;

	public MakePlaceSharedVisitor(String oldTemplateName, String oldPlaceName, String newTemplateName, String newPlaceName){
		Require.that(oldTemplateName != null, "oldTemplateName cannot be null");
		Require.that(oldPlaceName != null && !oldPlaceName.isEmpty(), "oldPlaceName cannot be null or empty");
		Require.that(newTemplateName != null, "newTemplateName cannot be null");
		Require.that(newPlaceName != null && !newPlaceName.isEmpty(), "newPlaceName cannot be null or empty");
		
		this.oldTemplateName = oldTemplateName;
		this.oldPlaceName = oldPlaceName;
		this.newTemplateName = newTemplateName;
		this.newPlaceName = newPlaceName;
	}
	
	@Override
	public void visit(TCTLPlaceNode node, Object context) {
		if(node.getTemplate().equals(oldTemplateName) && node.getPlace().equals(oldPlaceName)) {
			node.setTemplate(newTemplateName);
			node.setPlace(newPlaceName);
			if(context instanceof BooleanResult)
				((BooleanResult)context).setResult(true);
		}
			
	}
}
