package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.model.tapn.SharedTransition;

public class AddSharedTransitionCommand extends Command {
	private SharedTransition transition;
	private SharedTransitionsListModel listModel;
	
	public AddSharedTransitionCommand(SharedTransitionsListModel listModel, SharedTransition transition){
		this.listModel = listModel;
		this.transition = transition;
	}
	
	@Override
	public void redo() {
		listModel.addElement(transition);
	}

	@Override
	public void undo() {
		listModel.removeElement(transition);
	}

}
