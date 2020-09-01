package pipe.gui.undo;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.ConstantStore;

public class RemoveConstantEdit extends Command {

	private final Constant constant;
	private final ConstantStore store;

	public RemoveConstantEdit(Constant constant, ConstantStore store) {
		this.constant = constant;
		this.store = store;
	}

	@Override
	public void redo() {
		store.remove(constant);
		CreateGui.getCurrentTab().updateConstantsList();
	}

	@Override
	public void undo() {
		store.add(constant);
		CreateGui.getCurrentTab().updateConstantsList();
	}

}
