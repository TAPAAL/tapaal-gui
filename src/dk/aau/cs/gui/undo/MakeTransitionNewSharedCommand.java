package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;

public class MakeTransitionNewSharedCommand extends Command {
	private SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	private final String newName;
	private final String oldName;
	private final TimedArcPetriNet tapn;
	private final SharedPlacesAndTransitionsPanel sharedPanel;
	private final boolean multiShare;
	
	public MakeTransitionNewSharedCommand(TimedArcPetriNet tapn, String newName, TimedTransition timedTransition, TabContent tabContent, boolean multiShare){
		this.sharedTransition = null;
		this.tapn = tapn;
		this.timedTransition = timedTransition;
		this.newName = newName;
		this.oldName = timedTransition.name();
		this.sharedPanel = tabContent.getSharedPlacesAndTransitionsPanel();
		this.multiShare = multiShare;
	}
	
	@Override
	public void redo() {
		tapn.remove(timedTransition); // timedTransition has to be removed in case of reusing its name.
		if(sharedTransition == null){
			sharedTransition = new SharedTransition(newName);
		}
		sharedPanel.addSharedTransition(sharedTransition, multiShare);			
		sharedTransition.makeShared(timedTransition);
		tapn.add(timedTransition);
		
	}

	@Override
	public void undo() {
		tapn.add(timedTransition);
		
		if(sharedTransition != null){
			sharedPanel.removeSharedTransition(sharedTransition);
		}
		
		timedTransition.unshare();
		timedTransition.setName(oldName);
	}

}
