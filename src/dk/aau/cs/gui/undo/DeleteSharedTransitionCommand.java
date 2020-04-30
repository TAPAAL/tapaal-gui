package dk.aau.cs.gui.undo;

import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.model.tapn.SharedTransition;

public class DeleteSharedTransitionCommand extends Command {
	private final SharedTransition transition;
	private final SharedTransitionsListModel listModel;
	
	public DeleteSharedTransitionCommand(SharedTransition transition, SharedTransitionsListModel listModel){
		this.transition = transition;
		this.listModel = listModel;
	}
	
	@Override
	public void redo() {
		listModel.removeElement(transition);
	}

	@Override
	public void undo() {
		listModel.addElement(transition);
	}
}
