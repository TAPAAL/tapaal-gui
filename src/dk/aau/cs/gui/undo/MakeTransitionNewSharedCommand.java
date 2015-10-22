package dk.aau.cs.gui.undo;

import javax.swing.JOptionPane;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel;
import dk.aau.cs.gui.SharedTransitionNamePanel;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.RequireException;

public class MakeTransitionNewSharedCommand extends Command {
	private SharedTransition sharedTransition;
	private final TimedTransition timedTransition;
	private final String newName;
	private final String oldName;
	private final TimedArcPetriNet tapn;
	private SharedPlacesAndTransitionsPanel sharedPanel;
	
	public MakeTransitionNewSharedCommand(TimedArcPetriNet tapn, String newName, TimedTransition timedTransition, TabContent tabContent){
		this.sharedTransition = null;
		this.tapn = tapn;
		this.timedTransition = timedTransition;
		this.newName = newName;
		this.oldName = timedTransition.name();
		this.sharedPanel = tabContent.getSharedPlacesAndTransitionsPanel();
	}
	
	@Override
	public void redo() {
		tapn.remove(timedTransition); // timedTransition has to be removed in case of reusing its name.
		if(sharedTransition == null){
			sharedTransition = new SharedTransition(newName);
		}
		sharedPanel.addSharedTransition(sharedTransition);			
		sharedTransition.makeShared(timedTransition);
		
		if(!tapn.transitions().contains(timedTransition)){
			tapn.add(timedTransition); // adding it back in. Not sure this is the correct approach.
		}
	}

	@Override
	public void undo() {
		if(!tapn.transitions().contains(timedTransition)){
			tapn.add(timedTransition);
		}
		sharedPanel.removeSharedTransition(sharedTransition);				
		timedTransition.unshare();
		timedTransition.setName(oldName);
	}

}
