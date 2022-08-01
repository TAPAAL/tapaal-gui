package net.tapaal.gui.petrinet.undo;

import net.tapaal.gui.petrinet.editor.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.model.tapn.SharedTransition;

public class AddSharedTransitionCommand extends Command {
	private final SharedTransition transition;
	private final SharedTransitionsListModel listModel;
	
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
