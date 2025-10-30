package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel;
import pipe.gui.petrinet.PetriNetTab;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;

public class MakeTransitionNewSharedCommand implements Command {
	private SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	private final String newName;
	private final String oldName;
	private final TimedArcPetriNet tapn;
	private final SharedPlacesAndTransitionsPanel sharedPanel;
	private final boolean multiShare;
	
	public MakeTransitionNewSharedCommand(TimedArcPetriNet tapn, String newName, TimedTransition timedTransition, PetriNetTab tabContent, boolean multiShare){
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
		if (sharedTransition == null) {
			sharedTransition = new SharedTransition(newName);
		}

		sharedPanel.addSharedTransition(sharedTransition, multiShare);			
		sharedTransition.makeShared(timedTransition);
		tapn.add(timedTransition);
	}

	@Override
	public void undo() {
        tapn.remove(timedTransition);
		if (sharedTransition != null) {
			sharedPanel.removeSharedTransition(sharedTransition);
		}
		
		timedTransition.unshare();
		timedTransition.setName(oldName);

        tapn.add(timedTransition);
	}

}
