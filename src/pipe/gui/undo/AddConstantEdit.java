package pipe.gui.undo;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;

public class AddConstantEdit extends Command {
	private final Constant constant;
	private final ConstantStore store;

	public AddConstantEdit(Constant constant, ConstantStore store) {
		this.constant = constant;
		this.store = store;
	}

	@Override
	public void redo() {
		store.add(constant);
		CreateGui.getCurrentTab().updateConstantsList();
	}

	@Override
	public void undo() {
		store.remove(constant);
		CreateGui.getCurrentTab().updateConstantsList();
	}
}
